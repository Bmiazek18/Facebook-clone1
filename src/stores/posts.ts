import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { posts as postsData } from '@/data/posts';
import type { Post, Comment } from '@/types/Post';

export const usePostsStore = defineStore('posts', () => {
  const posts = ref<Post[]>(JSON.parse(JSON.stringify(postsData)));

  // Current user info (in real app this would come from auth)
  const currentUser = {
    id: 1,
    name: 'Bartosz Miazek',
    avatar: 'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg'
  }

  function addPost(post: Post) {
    posts.value.unshift(post); // Add new post to the beginning of the array
  }

  const sharePost = (originalPost: Post, comment: string) => {
    const newPost: Post = {
      id: `post_${Date.now()}`,
      authorId: currentUser.id,
      content: comment,
      date: new Date().toLocaleDateString(),
      timestamp: Date.now(),
      media: {
        images: [],
      },
      stats: {
          comments: 0,
          shares: 0,
      },
      reactions: {},
      context: {
          privacy: 'public'
      },
      sharedContent: {
          type: 'post',
          originalId: originalPost.id,
      }
    };
    addPost(newPost);
  }

  function findComment(comments: Comment[], commentId: number): Comment | null {
    for (const comment of comments) {
      if (comment.id === commentId) {
        return comment;
      }
      if (comment.replies) {
        const found = findComment(comment.replies, commentId);
        if (found) {
          return found;
        }
      }
    }
    return null;
  }

  function addComment(postId: string, comment: Comment, parentId: number | null) {
    const post = posts.value.find(p => p.id === postId);

    if (post) {
      if (parentId) {
        const parentComment = findComment(post.comments || [], parentId);

        if (parentComment) {
          if (!parentComment.replies) {
            parentComment.replies = [];
          }
          parentComment.replies.push(comment);
        }
      } else {
        if (!post.comments) {
          post.comments = [];
        }
        post.comments.push(comment);
      }
    }
  }

  function removePost(postId: string) {
    const index = posts.value.findIndex(p => p.id === postId);
    if (index !== -1) {
      posts.value.splice(index, 1);
    }
  }

  const getPostById = (postId: string) => {
    return posts.value.find(p => p.id === postId);
  }

  function handlePostReaction(postId: string, reaction: string | null) {
      const post = posts.value.find(p => p.id === postId);
      if (post) {
          const currentUserId = currentUser.id;
          if (!post.reactions) post.reactions = {};

    const newReactions: typeof post.reactions = {};
    let userCurrentReactionType: ReactionType | null = null;

    // First pass: Determine current user's reaction and populate newReactions without current user
          for (const type in post.reactions) {
        const userIds = post.reactions[type as keyof typeof post.reactions] || [];
        if (userIds.includes(currentUserId)) {
            userCurrentReactionType = type as ReactionType;
            // Filter out current user for this reaction type
            const filteredUserIds = userIds.filter(id => id !== currentUserId);
            if (filteredUserIds.length > 0) {
                newReactions[type as keyof typeof newReactions] = [...filteredUserIds];
            }
                  } else {
            // Copy other reactions as is (ensuring new array reference)
            if (userIds.length > 0) {
                 newReactions[type as keyof typeof newReactions] = [...userIds];
                  }
              }
          }

    // Second pass: Apply the new reaction logic
    if (reaction && reaction !== userCurrentReactionType) { // User is adding a new reaction or changing it
        const currentList = newReactions[reaction as keyof typeof newReactions] || [];
        newReactions[reaction as keyof typeof newReactions] = [...currentList, currentUserId];
    }
    // If reaction is null OR reaction is the same as userCurrentReactionType,
    // it means the user un-reacted (or clicked their current reaction to remove it).
    // In this case, `newReactions` already has the user removed from all reactions, which is correct.

    post.reactions = newReactions;
      }
  }

  function handleCommentReaction(postId: string, commentId: number, reaction: string | null) {
      const post = posts.value.find(p => p.id === postId);
      if (post && post.comments) {
          const comment = findComment(post.comments, commentId);
          if (comment) {
              // Update userReaction property
              comment.userReaction = reaction || undefined;

              // Update reactions count/list
              // Ensure reactions object exists
              if (!comment.reactions) comment.reactions = {};

              const currentUserId = currentUser.id;

               // Remove user from ALL previous reactions
              for (const type in comment.reactions) {
                  const userIds = comment.reactions[type as keyof typeof comment.reactions];
                  if (userIds && userIds.includes(currentUserId)) {
                       const newUserIds = userIds.filter(id => id !== currentUserId);
                       if (newUserIds.length === 0) {
                           delete comment.reactions[type as keyof typeof comment.reactions];
                       } else {
                           comment.reactions[type as keyof typeof comment.reactions] = newUserIds;
                       }
                  }
              }

              // Add new reaction
              if (reaction) {
                   const type = reaction as keyof typeof comment.reactions;
                   const currentList = comment.reactions[type] || [];
                   comment.reactions[type] = [...currentList, currentUserId];
              }
          }
      }
  }

  return {
    posts,
    currentUser,
    addPost,
    sharePost,
    addComment,
    removePost,
    getPostById,
    handlePostReaction,
    handleCommentReaction,
  }
})
