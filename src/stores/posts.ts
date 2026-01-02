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
      authorName: currentUser.name,
      authorAvatar: currentUser.avatar,
      authorId: currentUser.id,
      content: comment,
      date: new Date().toLocaleDateString(),
      timestamp: Date.now(),
      images: [],
      likesCount: 0,
      commentsCount: 0,
      sharesCount: 0,
      sharedFromId: originalPost.id,
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
          parentComment.replies.unshift(comment);
        }
      } else {
        if (!post.comments) {
          post.comments = [];
        }
        post.comments.unshift(comment);
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

  return {
    posts,
    currentUser,
    addPost,
    sharePost,
    addComment,
    removePost,
    getPostById,
  }
})
