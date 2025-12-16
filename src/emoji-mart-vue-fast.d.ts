declare module 'emoji-mart-vue-fast/src' {
  import { DefineComponent } from 'vue';
  
  export const Picker: DefineComponent<Record<string, unknown>, Record<string, unknown>, unknown>;
  export class EmojiIndex {
    constructor(data: Record<string, unknown>);
  }
}

declare module 'emoji-mart-vue-fast/data/all.json' {
  const data: Record<string, unknown>;
  export default data;
}
