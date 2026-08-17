import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import { keycloakify } from 'keycloakify/vite-plugin';
import path from 'path';

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    keycloakify({
      themeName: ["oidc-ui"],
      accountThemeImplementation: "none",
      extraThemeProperties: [
        "parent=base"
      ]
    })
  ],
  resolve: {
    alias: {
      '~': path.resolve(__dirname, './src')
    }
  }
});
