import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { PostData } from '@/types/StoryElement'
import { posts as postsData, type Post, type Comment } from '@/data/posts';

export interface SharedPost {
  id: string
  originalPost: PostData
  comment: string
  sharedBy: {
    name: string
    avatar: string
  }
  sharedAt: number
}

export const usePostsStore = defineStore('posts', () => {
  const posts = ref<Post[]>(JSON.parse(JSON.stringify(postsData)));
  const sharedPosts = ref<SharedPost[]>([])

  // Current user info (in real app this would come from auth)
  const currentUser = {
    name: 'Bartosz Miazek',
    avatar: 'https://i.pravatar.cc/150?img=12'
  }

  const addSharedPost = (originalPost: PostData, comment: string) => {
    const newPost: SharedPost = {
      id: `shared_${Date.now()}`,
      originalPost,
      comment,
      sharedBy: currentUser,
      sharedAt: Date.now()
    }
    sharedPosts.value.unshift(newPost) // Add to beginning of array
    return newPost
  }

  const removeSharedPost = (postId: string) => {
    const index = sharedPosts.value.findIndex(p => p.id === postId)
    if (index !== -1) {
      sharedPosts.value.splice(index, 1)
    }
  }

  const getSharedPosts = computed(() => sharedPosts.value)

  const getSharedPostsCount = computed(() => sharedPosts.value.length)

  function addPost(post: Post) {
    posts.value.unshift(post); // Add new post to the beginning of the array
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

  function addComment(postId: number, comment: Comment, parentId: number | null) {
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

  function removePost(postId: number) {
    const index = posts.value.findIndex(p => p.id === postId);
    if (index !== -1) {
      posts.value.splice(index, 1);
    }
  }

  return {
    posts,
    sharedPosts,
    currentUser,
    addSharedPost,
    removeSharedPost,
    getSharedPosts,
    getSharedPostsCount,
    addPost,
    addComment,
    removePost
  }
})
