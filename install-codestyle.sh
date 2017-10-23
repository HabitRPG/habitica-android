#!/bin/bash
# Installs Android's IntelliJ configs into your user configs.
# Script based on: https://github.com/square/java-code-styles/blob/master/install.sh

echo "Installing Android IntelliJ configs..."

for i in $HOME/Library/Preferences/IntelliJIdea*  \
         $HOME/Library/Preferences/IdeaIC*        \
         $HOME/Library/Preferences/AndroidStudio* \
         $HOME/.IntelliJIdea*/config              \
         $HOME/.IdeaIC*/config                    \
         $HOME/.AndroidStudio*/config
do
  if [[ -d $i ]]; then

    # Install codestyles
    mkdir -p $i/codestyles
    cp -fv AndroidStyle.xml $i/codestyles
  fi
done

echo "Done."
echo ""
echo "Restart IntelliJ and/or AndroidStudio, go to preferences, and apply 'HabiticaAndroidStyle'."
