export interface ProcessedPart {
  type: 'mention' | 'hashtag' | 'text';
  value: string;
  userId?: string;
  hashtag?: string;
}

export function processContent(content: string | undefined | null): ProcessedPart[] {
  if (!content) return [];

  // Regex for user mentions [@userId] and hashtags #hashtag
  const combinedRegex = /(\[@\d+\]|#[\w\u00C0-\u017F]+)/g;

  return content.split(combinedRegex)
    .filter(part => part) // Filter out empty strings
    .map((part): ProcessedPart => {
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

      // Plain text
      return {
        type: 'text',
        value: part
      };
    });
}
