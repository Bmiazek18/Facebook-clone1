import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getApolloClient } from '@/utils/apollo'
import { useAuthStore } from '@/stores/auth'
import type { Group, GroupRole } from '@/types/Group'
import {
  GET_GROUPS,
  GET_GROUP_BY_ID,
  GET_GROUP_OVERVIEW,
  CREATE_GROUP,
  JOIN_GROUP,
  LEAVE_GROUP,
  GET_GROUP_MEMBERSHIP,
  GET_PENDING_REQUESTS,
  GET_GROUP_MEMBERS,
  APPROVE_GROUP_REQUEST,
  REJECT_GROUP_REQUEST,
  REMOVE_GROUP_MEMBER,
  UPDATE_GROUP_MEMBER_ROLE,
  GET_GROUP_RULES,
  CREATE_GROUP_RULE,
  UPDATE_GROUP_RULES_ORDER,
  DELETE_GROUP_RULE,
  GET_GROUP_ACTIVITY_LOGS,
  LOG_GROUP_ACTIVITY
} from '@/graphql/groups'

export const useGroupsStore = defineStore('groups', () => {
  const groups = ref<Group[]>([])
  const authStore = useAuthStore()

  const mapGraphQLGroupToGroup = (g: any): Group => ({
    id: g.id,
    name: g.name,
    description: g.description || '',
    members: g.membersCount || 0,
    privacy: g.privacy || 'public',
    image: g.image || '',
    images: g.image ? [g.image] : [],
    lastActive: g.lastActive || '1 min temu',
    newPostsToday: g.newPostsToday || 0,
    newPostsMonth: g.newPostsMonth || 0,
    newMembersWeek: g.newMembersWeek || '',
    createdAge: g.createdAge || ''
  })

  const fetchGroups = async () => {
    try {
      const client = getApolloClient()
      const result = await client.query({
        query: GET_GROUPS,
        variables: { limit: 100, offset: 0 },
        fetchPolicy: 'network-only'
      })
      if (result?.data?.getGroups) {
        groups.value = result.data.getGroups.map(mapGraphQLGroupToGroup)
      }
    } catch (e) {
      console.error('Failed to fetch groups:', e)
    }
  }

  const getGroupById = (id: string) => {
    const existing = groups.value.find((g) => g.id === id)
    return existing
  }

  const loadGroupDetails = async (id: string) => {
    try {
      const client = getApolloClient()
      const result = await client.query({
        query: GET_GROUP_BY_ID,
        variables: { id },
        fetchPolicy: 'network-only'
      })
      if (result?.data?.getGroupById) {
        const g = result.data.getGroupById
        const groupObj = mapGraphQLGroupToGroup(g)
        const index = groups.value.findIndex((grp) => grp.id === id)
        if (index !== -1) {
          groups.value[index] = groupObj
        } else {
          groups.value.push(groupObj)
        }
        return groupObj
      }
    } catch (e) {
      console.error('Failed to load group details:', e)
    }
    return undefined
  }

  const addGroup = async (groupInput: Omit<Group, 'id' | 'members'>) => {
    try {
      const client = getApolloClient()
      const result = await client.mutate({
        mutation: CREATE_GROUP,
        variables: {
          input: {
            name: groupInput.name,
            description: groupInput.description || '',
            privacy: groupInput.privacy,
            image: groupInput.image || '',
            creatorId: authStore.currentUserId
          }
        }
      })
      if (result?.data?.createGroup) {
        const g = result.data.createGroup
        const newGroup = mapGraphQLGroupToGroup(g)
        groups.value.push(newGroup)
        return newGroup
      }
    } catch (e) {
      console.error('Failed to add group:', e)
    }
    return null
  }

  const joinGroup = async (groupId: string) => {
    try {
      const client = getApolloClient()
      const result = await client.mutate({
        mutation: JOIN_GROUP,
        variables: {
          groupId,
          userId: authStore.currentUserId
        }
      })
      if (result?.data?.joinGroup) {
        const grp = groups.value.find((g) => g.id === groupId)
        if (grp) {
          if (grp.privacy !== 'private') {
            grp.members++
          }
        }
        return true
      }
    } catch (e) {
      console.error('Failed to join group:', e)
    }
    return false
  }

  const leaveGroup = async (groupId: string) => {
    try {
      const client = getApolloClient()
      const result = await client.mutate({
        mutation: LEAVE_GROUP,
        variables: {
          groupId,
          userId: authStore.currentUserId
        }
      })
      if (result?.data?.leaveGroup) {
        const grp = groups.value.find((g) => g.id === groupId)
        if (grp) grp.members = Math.max(0, grp.members - 1)
        return true
      }
    } catch (e) {
      console.error('Failed to leave group:', e)
    }
    return false
  }

  const getGroupMembership = async (groupId: string, userId: string) => {
    try {
      const client = getApolloClient()
      const result = await client.query({
        query: GET_GROUP_MEMBERSHIP,
        variables: { groupId, userId },
        fetchPolicy: 'network-only'
      })
      return result?.data?.getGroupMembership || ''
    } catch (e) {
      console.error('Failed to get group membership:', e)
      return ''
    }
  }

  const getPendingRequests = async (groupId: string) => {
    try {
      const client = getApolloClient()
      const result = await client.query({
        query: GET_PENDING_REQUESTS,
        variables: { groupId },
        fetchPolicy: 'network-only'
      })
      return result?.data?.getPendingRequests || []
    } catch (e) {
      console.error('Failed to get pending requests:', e)
      return []
    }
  }

  const approveGroupRequest = async (groupId: string, userId: string) => {
    try {
      const client = getApolloClient()
      const result = await client.mutate({
        mutation: APPROVE_GROUP_REQUEST,
        variables: {
          groupId,
          userId,
          adminId: authStore.currentUserId
        }
      })
      if (result?.data?.approveGroupRequest) {
        const grp = groups.value.find((g) => g.id === groupId)
        if (grp) grp.members++
        return true
      }
    } catch (e) {
      console.error('Failed to approve group request:', e)
    }
    return false
  }

  const rejectGroupRequest = async (groupId: string, userId: string) => {
    try {
      const client = getApolloClient()
      const result = await client.mutate({
        mutation: REJECT_GROUP_REQUEST,
        variables: {
          groupId,
          userId,
          adminId: authStore.currentUserId
        }
      })
      return !!result?.data?.rejectGroupRequest
    } catch (e) {
      console.error('Failed to reject group request:', e)
    }
    return false
  }

  const fetchGroupMembers = async (groupId: string) => {
    try {
      const client = getApolloClient()
      const result = await client.query({
        query: GET_GROUP_MEMBERS,
        variables: { groupId },
        fetchPolicy: 'network-only'
      })
      return result?.data?.getGroupMembers || []
    } catch (e) {
      console.error('Failed to fetch group members:', e)
      return []
    }
  }

  const removeGroupMember = async (groupId: string, userId: string) => {
    try {
      const client = getApolloClient()
      const result = await client.mutate({
        mutation: REMOVE_GROUP_MEMBER,
        variables: {
          groupId,
          userId,
          adminId: authStore.currentUserId
        }
      })
      if (result?.data?.removeGroupMember) {
        const grp = groups.value.find((g) => g.id === groupId)
        if (grp) grp.members = Math.max(0, grp.members - 1)
        return true
      }
    } catch (e) {
      console.error('Failed to remove group member:', e)
    }
    return false
  }

  const updateGroupMemberRole = async (groupId: string, userId: string, role: GroupRole | string) => {
    try {
      const client = getApolloClient()
      const result = await client.mutate({
        mutation: UPDATE_GROUP_MEMBER_ROLE,
        variables: {
          groupId,
          userId,
          role,
          adminId: authStore.currentUserId
        }
      })
      return !!result?.data?.updateGroupMemberRole
    } catch (e) {
      console.error('Failed to update group member role:', e)
    }
    return false
  }

  const getGroupOverview = async (groupId: string) => {
    try {
      const client = getApolloClient()
      const result = await client.query({
        query: GET_GROUP_OVERVIEW,
        variables: { groupId },
        fetchPolicy: 'network-only'
      })
      return result?.data?.getGroupOverview || null
    } catch (e) {
      console.error('Failed to get group overview:', e)
      return null
    }
  }

  const fetchGroupRules = async (groupId: string) => {
    try {
      const client = getApolloClient()
      const result = await client.query({
        query: GET_GROUP_RULES,
        variables: { groupId },
        fetchPolicy: 'network-only'
      })
      return result?.data?.getGroupRules || []
    } catch (e) {
      console.error('Failed to fetch group rules:', e)
      return []
    }
  }

  const createGroupRule = async (groupId: string, title: string, description: string) => {
    try {
      const client = getApolloClient()
      const result = await client.mutate({
        mutation: CREATE_GROUP_RULE,
        variables: { groupId, title, description }
      })
      return result?.data?.createGroupRule
    } catch (e) {
      console.error('Failed to create group rule:', e)
      return null
    }
  }

  const updateGroupRulesOrder = async (groupId: string, ruleIds: string[]) => {
    try {
      const client = getApolloClient()
      const result = await client.mutate({
        mutation: UPDATE_GROUP_RULES_ORDER,
        variables: { groupId, ruleIds }
      })
      return !!result?.data?.updateGroupRulesOrder
    } catch (e) {
      console.error('Failed to update group rules order:', e)
      return false
    }
  }

  const deleteGroupRule = async (ruleId: string) => {
    try {
      const client = getApolloClient()
      const result = await client.mutate({
        mutation: DELETE_GROUP_RULE,
        variables: { ruleId }
      })
      return !!result?.data?.deleteGroupRule
    } catch (e) {
      console.error('Failed to delete group rule:', e)
      return false
    }
  }

  const fetchGroupActivityLogs = async (groupId: string) => {
    try {
      const client = getApolloClient()
      const result = await client.query({
        query: GET_GROUP_ACTIVITY_LOGS,
        variables: { groupId },
        fetchPolicy: 'network-only'
      })
      return result?.data?.getGroupActivityLogs || []
    } catch (e) {
      console.error('Failed to fetch group activity logs:', e)
      return []
    }
  }

  const logGroupActivity = async (groupId: string, text: string, note?: string) => {
    try {
      const client = getApolloClient()
      const actorId = authStore.currentUserId || '1'
      const actorName = [authStore.currentUser?.firstName, authStore.currentUser?.lastName]
        .filter(Boolean)
        .join(' ') || 'Test Testowy'

      const result = await client.mutate({
        mutation: LOG_GROUP_ACTIVITY,
        variables: {
          groupId,
          text,
          note: note || '',
          actorId,
          actorName
        }
      })
      return result?.data?.logGroupActivity
    } catch (e) {
      console.error('Failed to log group activity:', e)
      return null
    }
  }

  return {
    groups,
    fetchGroups,
    getGroupById,
    loadGroupDetails,
    addGroup,
    joinGroup,
    leaveGroup,
    getGroupMembership,
    getPendingRequests,
    approveGroupRequest,
    rejectGroupRequest,
    fetchGroupMembers,
    removeGroupMember,
    updateGroupMemberRole,
    getGroupOverview,
    fetchGroupRules,
    createGroupRule,
    updateGroupRulesOrder,
    deleteGroupRule,
    fetchGroupActivityLogs,
    logGroupActivity
  }
})
