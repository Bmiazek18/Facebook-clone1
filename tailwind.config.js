import type { Config } from 'tailwindcss'

import scrollbar from 'tailwind-scrollbar'

const config: Config = {
  content: [
    "./index.html",
    "./src/**/*.{vue,ts,tsx,js,jsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [scrollbar],
}

export default config
