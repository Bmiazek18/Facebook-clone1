export interface ProcessedContent {
  type: 'mention' | 'hashtag' | 'text' | 'link';
  value: string;
  userId?: string;
  hashtag?: string;
  url?: string;
}

export function processContent(content: string | undefined | null): ProcessedContent[] {
  if (!content) return [];

  // Regex for user mentions [@userId], hashtags #hashtag, and URLs
  const combinedRegex = /(\[@\d+\]|#[\w\u00C0-\u017F]+|https?:\/\/[^\s]+)/g;

  return content.split(combinedRegex)
    .filter(part => part) // Filter out empty strings
    .map((part): ProcessedContent => {
      // User Mention [@userId]
      if (part.startsWith('[@') && part.endsWith(']')) {
        const userId = part.slice(2, -1);
        return {
          type: 'mention',
          value: part,
          userId: userId
        };
      }

      // Hashtag #word
      if (part.startsWith('#')) {
        return {
          type: 'hashtag',
          value: part,
          hashtag: part.substring(1),
        };
      }

      // URL
      if (part.startsWith('https')) {
        return {
          type: 'link',
          value: part,
          url: part,
        };
      }

      // Plain text
      return {
        type: 'text',
        value: part
      };
    });
}
