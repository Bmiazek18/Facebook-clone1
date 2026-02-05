import { defineStore } from 'pinia'
import { ref } from 'vue'
import { posts as postsData } from '@/data/posts';
import type { Post, Comment, ReactionType } from '@/types/Post';

export const usePostsStore = defineStore('posts', () => {
  const posts = ref<Post[]>(JSON.parse(JSON.stringify(postsData)));

  const currentUser = {
    id: 1,
    name: 'Bartosz Miazek',
    avatar: 'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg'
  }

  // --- Helpers ---
  function findComment(comments: Comment[], commentId: number): Comment | null {
    for (const comment of comments) {
      if (comment.id === commentId) return comment;
      if (comment.replies) {
        const found = findComment(comment.replies, commentId);
        if (found) return found;
      }
    }
    return null;
  }

  // --- Post Actions ---
  function addPost(post: Post) {
    posts.value.unshift(post);
  }

  function removePost(postId: string) {
    posts.value = posts.value.filter(p => p.id !== postId);
  }

  const getPostById = (postId: string) => posts.value.find(p => p.id === postId);

function handlePostReaction(postId: string, reaction: ReactionType | null) {
  // 1. Pobieramy REFERENCJĘ do obiektu (to kluczowe - pracujemy na tym samym obiekcie w pamięci)
  const post = posts.value.find(p => p.id === postId);

  // Jeśli post nie istnieje, kończymy
  if (!post) return;

  const userId = currentUser.id;

  // 2. Budujemy nową strukturę reakcji "na boku" (czysty JS)
  // Kopiujemy obecne reakcje, żeby nie modyfikować ich w pętli
  const currentReactions = { ...(post.reactions || {}) };
  const nextReactions: Record<string, number[]> = {};

  // Przepisujemy stare reakcje, pomijając obecnego użytkownika (czyszczenie)
  Object.keys(currentReactions).forEach((type) => {
    const rType = type as ReactionType;
    // Filtrujemy ID użytkownika z danej reakcji
    const filteredIds = (currentReactions[rType] || []).filter(id => id !== userId);

    // Jeśli po usunięciu użytkownika zostały jakieś inne osoby, zachowujemy tę reakcję
    if (filteredIds.length > 0) {
      nextReactions[rType] = filteredIds;
    }
  });

  // 3. Dodajemy nową reakcję (jeśli użytkownik coś wybrał)
  if (reaction) {
    // Inicjalizujemy tablicę jeśli nie istnieje
    if (!nextReactions[reaction]) {
      nextReactions[reaction] = [];
    }
    // Dodajemy użytkownika
    nextReactions[reaction].push(userId);
  }

  // 4. CRITICAL FIX:
  // NIE ROBIMY: posts.value[index] = ... (To powoduje błąd "emitsOptions")
  // ROBIMY: Przypisujemy nowe reakcje do ISTNIEJĄCEGO obiektu post
  post.reactions = nextReactions;
}

  function sharePost(originalPost: Post, comment: string) {
    if (!originalPost) return

    const newPost: Post = {
      id: `post_${Date.now()}_${currentUser.id}`,
      authorId: currentUser.id,
      content: comment,
      timestamp: Date.now(),
      date: new Date().toISOString(),
      reactions: {},
      comments: [],
      stats: {
        comments: 0,
        shares: 0,
      },
      sharedContent: {
        type: 'post',
        originalId: originalPost.id,
      },
      context: {
        privacy: 'public',
      },
    }

    // Opcjonalnie: zwiększ licznik udostępnień w oryginalnym poście
    const postToUpdate = getPostById(originalPost.id)
    if (postToUpdate && postToUpdate.stats) {
      postToUpdate.stats.shares = (postToUpdate.stats.shares || 0) + 1
    }

    addPost(newPost)
  }

  // --- Comment Actions ---
  function addComment(postId: string, comment: Comment, parentId: number | null) {
    const post = posts.value.find(p => p.id === postId);
    if (!post) return;

    if (parentId) {
      const parentComment = findComment(post.comments || [], parentId);
      if (parentComment) {
        if (!parentComment.replies) parentComment.replies = [];
        parentComment.replies.push(comment);
      }
    } else {
      if (!post.comments) post.comments = [];
      post.comments.push(comment);
    }
  }

  function handleCommentReaction(postId: string, commentId: number, reaction: ReactionType | null) {
    const post = posts.value.find(p => p.id === postId);
    if (!post || !post.comments) return;

    const comment = findComment(post.comments, commentId);
    if (!comment) return;

    const userId = currentUser.id;
    if (!comment.reactions) comment.reactions = {};

    // Usuwamy starą reakcję
    Object.keys(comment.reactions).forEach((type) => {
      const rType = type as ReactionType;
      comment.reactions![rType] = comment.reactions![rType]!.filter(id => id !== userId);
      if (comment.reactions![rType]!.length === 0) {
        delete comment.reactions![rType];
      }
    });

    // Dodajemy nową
    if (reaction) {
      if (!comment.reactions[reaction]) comment.reactions[reaction] = [];
      comment.reactions[reaction]!.push(userId);
    }

    // Opcjonalnie: zsynchronizuj userReaction (jeśli Twój interfejs tego używa)
    comment.userReaction = reaction || undefined;
  }

  return {
    posts,
    currentUser,
    addPost,
    addComment,
    removePost,
    findComment,
    getPostById,
    handlePostReaction,
    handleCommentReaction,
    sharePost,
  }
})
