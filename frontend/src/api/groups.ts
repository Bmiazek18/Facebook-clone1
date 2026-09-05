import { apiClient } from './client'
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

export const mapGraphQLGroupToGroup = (g: any): Group => ({
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

export const groupsApi = {
  async getGroups(limit = 100, offset = 0): Promise<Group[]> {
    const data = await apiClient.query<{ getGroups: any[] }>(
      GET_GROUPS,
      { limit, offset },
      { fetchPolicy: 'network-only' }
    )
    return (data?.getGroups || []).map(mapGraphQLGroupToGroup)
  },

  async getGroupById(id: string): Promise<Group | null> {
    const data = await apiClient.query<{ getGroupById: any }>(
      GET_GROUP_BY_ID,
      { id },
      { fetchPolicy: 'network-only' }
    )
    return data?.getGroupById ? mapGraphQLGroupToGroup(data.getGroupById) : null
  },

  async createGroup(input: { name: string; description?: string; privacy: string; image?: string; creatorId?: string }): Promise<Group | null> {
    const data = await apiClient.mutate<{ createGroup: any }>(
      CREATE_GROUP,
      { input }
    )
    return data?.createGroup ? mapGraphQLGroupToGroup(data.createGroup) : null
  },

  async joinGroup(groupId: string, userId?: string): Promise<boolean> {
    const data = await apiClient.mutate<{ joinGroup: boolean }>(
      JOIN_GROUP,
      { groupId, userId }
    )
    return !!data?.joinGroup
  },

  async leaveGroup(groupId: string, userId?: string): Promise<boolean> {
    const data = await apiClient.mutate<{ leaveGroup: boolean }>(
      LEAVE_GROUP,
      { groupId, userId }
    )
    return !!data?.leaveGroup
  },

  async getMembership(groupId: string, userId: string): Promise<string> {
    const data = await apiClient.query<{ getGroupMembership: string }>(
      GET_GROUP_MEMBERSHIP,
      { groupId, userId },
      { fetchPolicy: 'network-only' }
    )
    return data?.getGroupMembership || ''
  },

  async getPendingRequests(groupId: string): Promise<any[]> {
    const data = await apiClient.query<{ getPendingRequests: any[] }>(
      GET_PENDING_REQUESTS,
      { groupId },
      { fetchPolicy: 'network-only' }
    )
    return data?.getPendingRequests || []
  },

  async approveRequest(groupId: string, userId: string, adminId?: string): Promise<boolean> {
    const data = await apiClient.mutate<{ approveGroupRequest: boolean }>(
      APPROVE_GROUP_REQUEST,
      { groupId, userId, adminId }
    )
    return !!data?.approveGroupRequest
  },

  async rejectRequest(groupId: string, userId: string, adminId?: string): Promise<boolean> {
    const data = await apiClient.mutate<{ rejectGroupRequest: boolean }>(
      REJECT_GROUP_REQUEST,
      { groupId, userId, adminId }
    )
    return !!data?.rejectGroupRequest
  },

  async getMembers(groupId: string): Promise<any[]> {
    const data = await apiClient.query<{ getGroupMembers: any[] }>(
      GET_GROUP_MEMBERS,
      { groupId },
      { fetchPolicy: 'network-only' }
    )
    return data?.getGroupMembers || []
  },

  async removeMember(groupId: string, userId: string, adminId?: string): Promise<boolean> {
    const data = await apiClient.mutate<{ removeGroupMember: boolean }>(
      REMOVE_GROUP_MEMBER,
      { groupId, userId, adminId }
    )
    return !!data?.removeGroupMember
  },

  async updateMemberRole(groupId: string, userId: string, role: GroupRole | string, adminId?: string): Promise<boolean> {
    const data = await apiClient.mutate<{ updateGroupMemberRole: boolean }>(
      UPDATE_GROUP_MEMBER_ROLE,
      { groupId, userId, role, adminId }
    )
    return !!data?.updateGroupMemberRole
  },

  async getOverview(groupId: string): Promise<any> {
    const data = await apiClient.query<{ getGroupOverview: any }>(
      GET_GROUP_OVERVIEW,
      { groupId },
      { fetchPolicy: 'network-only' }
    )
    return data?.getGroupOverview || null
  },

  async getRules(groupId: string): Promise<any[]> {
    const data = await apiClient.query<{ getGroupRules: any[] }>(
      GET_GROUP_RULES,
      { groupId },
      { fetchPolicy: 'network-only' }
    )
    return data?.getGroupRules || []
  },

  async createRule(groupId: string, title: string, description: string): Promise<any> {
    const data = await apiClient.mutate<{ createGroupRule: any }>(
      CREATE_GROUP_RULE,
      { groupId, title, description }
    )
    return data?.createGroupRule || null
  },

  async updateRulesOrder(groupId: string, ruleIds: string[]): Promise<boolean> {
    const data = await apiClient.mutate<{ updateGroupRulesOrder: boolean }>(
      UPDATE_GROUP_RULES_ORDER,
      { groupId, ruleIds }
    )
    return !!data?.updateGroupRulesOrder
  },

  async deleteRule(ruleId: string): Promise<boolean> {
    const data = await apiClient.mutate<{ deleteGroupRule: boolean }>(
      DELETE_GROUP_RULE,
      { ruleId }
    )
    return !!data?.deleteGroupRule
  },

  async getActivityLogs(groupId: string): Promise<any[]> {
    const data = await apiClient.query<{ getGroupActivityLogs: any[] }>(
      GET_GROUP_ACTIVITY_LOGS,
      { groupId },
      { fetchPolicy: 'network-only' }
    )
    return data?.getGroupActivityLogs || []
  },

  async logActivity(input: { groupId: string; text: string; note?: string; actorId?: string; actorName?: string }): Promise<any> {
    const data = await apiClient.mutate<{ logGroupActivity: any }>(
      LOG_GROUP_ACTIVITY,
      {
        groupId: input.groupId,
        text: input.text,
        note: input.note || '',
        actorId: input.actorId || '1',
        actorName: input.actorName || 'User'
      }
    )
    return data?.logGroupActivity || null
  }
}
