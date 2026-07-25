import type { CodegenConfig } from '@graphql-codegen/cli'

const config: CodegenConfig = {
  overwrite: true,
  schema: [
    '../feed-edge-service/src/main/resources/schema/schema.graphqls',
    '../user-edge-service/src/main/resources/schema/schema.graphqls',
    '../socialgraph-edge-service/src/main/resources/schema/schema.graphqls'
  ],
  documents: ['src/graphql/**/*.ts'],
  ignoreNoDocuments: true,
  generates: {
    'src/gql/': {
      preset: 'client',
      config: {
        useTypeImports: true
      }
    }
  }
}

export default config
