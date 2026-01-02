import { ref, watch, nextTick, computed, onMounted } from 'vue';
import type { Ref } from 'vue';
import { useUserSearch } from './useUserSearch';
import { getAllUsers, type User } from '@/data/users';

export function useContentEditable(contentEditableDiv: Ref<HTMLDivElement | null>, postContent: Ref<string>) {
    const isLocalUpdate = ref(false); // Flag to prevent infinite loop
    
    const searchTerm = ref<string | null>(null);
    const { matchingUsers } = useUserSearch(searchTerm);
    const showUserDropdown = computed(() => matchingUsers.value.length > 0 && searchTerm.value !== null);

    // Watch postContent for external changes and render them
    watch(postContent, () => {
        if (!isLocalUpdate.value) {
            renderContentEditable();
        }
    });

    // --- Core ContentEditable Logic ---
    function onContentInput() {
        if (!contentEditableDiv.value) return;
        isLocalUpdate.value = true;

        let newContent = '';
        const nodes = contentEditableDiv.value.childNodes;

        nodes.forEach(node => {
            if (node.nodeType === Node.TEXT_NODE) {
                newContent += node.textContent;
            } else if (node.nodeType === Node.ELEMENT_NODE) {
                const el = node as HTMLElement;
                if (el.dataset.userId) {
                    newContent += `[@${el.dataset.userId}]`;
                } else if (el.tagName === 'BR') {
                    newContent += '\n';
                } else {
                    newContent += el.innerText;
                }
            }
        });

        postContent.value = newContent;
        handleUserTagging();

        nextTick(() => {
            isLocalUpdate.value = false;
        });
    }

    function renderContentEditable() {
        if (!contentEditableDiv.value) return;

        let htmlContent = postContent.value || '';
        htmlContent = htmlContent
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/\n/g, '<br>');

        const allUsers = getAllUsers();

        htmlContent = htmlContent.replace(/\[@(\d+)\]/g, (match, userId) => {
            const user = allUsers.find(u => u.id === parseInt(userId));
            if (user) {
                return `<span contenteditable="false" class="bg-blue-100 text-blue-600 font-semibold px-1 rounded mx-0.5" data-user-id="${user.id}">@${user.name}</span>`;
            }
            return match;
        });
        
        // Only update innerHTML if it's different to prevent unnecessary re-renders and cursor issues
        if (contentEditableDiv.value.innerHTML !== htmlContent) {
            contentEditableDiv.value.innerHTML = htmlContent;
            moveCursorToEnd();
        }
    }

    function moveCursorToEnd() {
        nextTick(() => {
            if (contentEditableDiv.value) {
                const selection = window.getSelection();
                const range = document.createRange();
                range.selectNodeContents(contentEditableDiv.value);
                range.collapse(false);
                selection?.removeAllRanges();
                selection?.addRange(range);
                contentEditableDiv.value.focus();
            }
        });
    }

    // --- User Tagging Logic (moved from component) ---
    function handleUserTagging() {
        const selection = window.getSelection();
        if (selection && selection.rangeCount > 0) {
            const range = selection.getRangeAt(0);
            if (range.startContainer.nodeType === Node.TEXT_NODE) {
                const textNode = range.startContainer;
                const textContent = textNode.textContent || '';
                const textBeforeCaret = textContent.substring(0, range.startOffset);
                const match = textBeforeCaret.match(/@([^\s]*)$/);

                if (match) {
                    searchTerm.value = match[1];
                } else {
                    searchTerm.value = null;
                }
            } else {
                searchTerm.value = null;
            }
        }
    }

    function selectUser(user: User) {
        if (!contentEditableDiv.value) return;

        const currentText = postContent.value;
        const newContent = currentText.replace(/@([^\s]*)$/, `[@${user.id}] `);
        postContent.value = newContent;
        searchTerm.value = null;

        nextTick(() => {
            moveCursorToEnd();
        });
    }

    function addEmoji(emoji: any) {
        postContent.value += emoji.native;
    }

    return {
        postContent, // Expose postContent so component can watch it
        onContentInput,
        matchingUsers,
        showUserDropdown,
        selectUser,
        addEmoji,
        renderContentEditable, // Expose for initial rendering on mount
        moveCursorToEnd, // Expose for focus management if needed
    };
}