import { defineStore } from 'pinia'
import { ref } from 'vue'
import { groupsApi } from '@/api/groups'
import { useAuthStore } from '@/stores/auth'
import type { Group, GroupRole } from '@/types/Group'

export const useGroupsStore = defineStore('groups', () => {
  const groups = ref<Group[]>([])
  const userGroups = ref<Group[]>([])
  const authStore = useAuthStore()

  const fetchGroups = async () => {
    try {
      groups.value = await groupsApi.getGroups(100, 0)
    } catch (e) {
      console.error('Failed to fetch groups:', e)
    }
  }

  const fetchUserGroups = async (userId?: string) => {
    const effectiveUserId = userId || String(authStore.currentUserId)
    if (!effectiveUserId || effectiveUserId === '0') return []
    try {
      const result = await groupsApi.getUserGroups(effectiveUserId)
      userGroups.value = result
      return result
    } catch (e) {
      console.error('Failed to fetch user groups:', e)
      return []
    }
  }

  const getGroupById = (id: string) => {
    const existing = groups.value.find((g) => g.id === id)
    return existing
  }

  const loadGroupDetails = async (id: string) => {
    try {
      const groupObj = await groupsApi.getGroupById(id)
      if (groupObj) {
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
      const newGroup = await groupsApi.createGroup({
        name: groupInput.name,
        description: groupInput.description || '',
        privacy: groupInput.privacy,
        image: groupInput.image || '',
        creatorId: authStore.currentUserId
      })
      if (newGroup) {
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
      const success = await groupsApi.joinGroup(groupId, authStore.currentUserId)
      if (success) {
        const grp = groups.value.find((g) => g.id === groupId)
        if (grp && grp.privacy !== 'private') {
          grp.members++
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
      const success = await groupsApi.leaveGroup(groupId, authStore.currentUserId)
      if (success) {
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
      return await groupsApi.getMembership(groupId, userId)
    } catch (e) {
      console.error('Failed to get group membership:', e)
      return ''
    }
  }

  const getPendingRequests = async (groupId: string) => {
    try {
      return await groupsApi.getPendingRequests(groupId)
    } catch (e) {
      console.error('Failed to get pending requests:', e)
      return []
    }
  }

  const approveGroupRequest = async (groupId: string, userId: string) => {
    try {
      const success = await groupsApi.approveRequest(groupId, userId, authStore.currentUserId)
      if (success) {
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
      return await groupsApi.rejectRequest(groupId, userId, authStore.currentUserId)
    } catch (e) {
      console.error('Failed to reject group request:', e)
    }
    return false
  }

  const fetchGroupMembers = async (groupId: string) => {
    try {
      return await groupsApi.getMembers(groupId)
    } catch (e) {
      console.error('Failed to fetch group members:', e)
      return []
    }
  }

  const removeGroupMember = async (groupId: string, userId: string) => {
    try {
      const success = await groupsApi.removeMember(groupId, userId, authStore.currentUserId)
      if (success) {
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
      return await groupsApi.updateMemberRole(groupId, userId, role, authStore.currentUserId)
    } catch (e) {
      console.error('Failed to update group member role:', e)
    }
    return false
  }

  const getGroupOverview = async (groupId: string) => {
    try {
      return await groupsApi.getOverview(groupId)
    } catch (e) {
      console.error('Failed to get group overview:', e)
      return null
    }
  }

  const fetchGroupRules = async (groupId: string) => {
    try {
      return await groupsApi.getRules(groupId)
    } catch (e) {
      console.error('Failed to fetch group rules:', e)
      return []
    }
  }

  const createGroupRule = async (groupId: string, title: string, description: string) => {
    try {
      return await groupsApi.createRule(groupId, title, description)
    } catch (e) {
      console.error('Failed to create group rule:', e)
      return null
    }
  }

  const updateGroupRulesOrder = async (groupId: string, ruleIds: string[]) => {
    try {
      return await groupsApi.updateRulesOrder(groupId, ruleIds)
    } catch (e) {
      console.error('Failed to update group rules order:', e)
      return false
    }
  }

  const deleteGroupRule = async (ruleId: string) => {
    try {
      return await groupsApi.deleteRule(ruleId)
    } catch (e) {
      console.error('Failed to delete group rule:', e)
      return false
    }
  }

  const fetchGroupActivityLogs = async (groupId: string) => {
    try {
      return await groupsApi.getActivityLogs(groupId)
    } catch (e) {
      console.error('Failed to fetch group activity logs:', e)
      return []
    }
  }

  const logGroupActivity = async (groupId: string, text: string, note?: string) => {
    try {
      const actorName = [authStore.currentUser?.firstName, authStore.currentUser?.lastName]
        .filter(Boolean)
        .join(' ') || 'Test Testowy'

      return await groupsApi.logActivity({
        groupId,
        text,
        note: note || '',
        actorId: authStore.currentUserId || '1',
        actorName
      })
    } catch (e) {
      console.error('Failed to log group activity:', e)
      return null
    }
  }

  return {
    groups,
    userGroups,
    fetchGroups,
    fetchUserGroups,
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
