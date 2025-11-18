/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  readonly VITE_FIREBASE_API_KEY: string
  readonly VITE_FIREBASE_STORAGE_BUCKET: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
