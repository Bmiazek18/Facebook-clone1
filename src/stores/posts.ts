import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { posts as postsData, type Post, type Comment } from '@/data/posts';

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
          
          // Initialize reactions if missing
          if (!post.reactions) post.reactions = {};

          // Remove user from ALL previous reactions
          for (const type in post.reactions) {
              const userIds = post.reactions[type as keyof typeof post.reactions];
              if (userIds && userIds.includes(currentUserId)) {
                  // Filter out the current user to create a NEW array reference (triggers reactivity)
                  const newUserIds = userIds.filter(id => id !== currentUserId);
                  
                  if (newUserIds.length === 0) {
                      delete post.reactions[type as keyof typeof post.reactions];
                  } else {
                      post.reactions[type as keyof typeof post.reactions] = newUserIds;
                  }
              }
          }

          // Add new reaction if it's not null (null means un-react)
          if (reaction) {
              const type = reaction as keyof typeof post.reactions;
              const currentList = post.reactions[type] || [];
              // Create a NEW array with the added user ID
              post.reactions[type] = [...currentList, currentUserId];
          }
      }
  }

  function handleCommentReaction(postId: string, commentId: number, reaction: string | null, oldReaction: string | null) {
    // ... (This function looks fine, keeping it for now but note that Comment interface might need similar update if we want detailed reactions there too)
    const post = posts.value.find(p => p.id === postId);
    // ... implementation ...
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
