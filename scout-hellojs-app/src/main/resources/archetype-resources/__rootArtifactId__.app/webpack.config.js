const baseConfig = require('@eclipse-scout/cli/scripts/webpack-defaults');

module.exports = (env, args) => {
  args.resDirArray = ['src/main/resources/WebContent', 'node_modules/@eclipse-scout/core/res'];
  const config = baseConfig(env, args);

  config.entry = {
    '${simpleArtifactName}': './src/main/js/index.js',
    '${simpleArtifactName}-theme': require.resolve('@${simpleArtifactName}/ui/src/main/js/theme.less'),
    '${simpleArtifactName}-theme-dark': require.resolve('@${simpleArtifactName}/ui/src/main/js/theme-dark.less')
  };

  return config;
};
