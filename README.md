# Habitica for Android

[![Join the chat at https://gitter.im/HabitRPG/habitrpg-android](https://badges.gitter.im/HabitRPG/habitrpg-android.svg)](https://gitter.im/HabitRPG/habitrpg-android?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[Habitica](https://habitica.com) is an open source habit building program which treats your life like a Role Playing Game. Level up as you succeed, lose HP as you fail, earn money to buy weapons and armor. This repository is related to the Android Native Application.

It's also on Google Play:

<a href="https://play.google.com/store/apps/details?id=com.habitrpg.android.habitica">
  <img alt="Get it on Google Play"
       width="185"
       src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" />
</a>

Having the application installed is a good way to be notified of new releases. However, clicking "Watch" on this
repository will allow GitHub to email you whenever we publish a release.


# What's New

See the project's Releases page for a list of versions with their changelogs.

##### [View Releases](https://github.com/HabitRPG/habitrpg-android/releases)

If you Watch this repository, GitHub will send you an email every time we publish an update.

## Contributing

For an introduction to the technologies used and how the software is organized, refer to [Contributing to Habitica](http://habitica.wikia.com/wiki/Contributing_to_Habitica#Coders_.28Web_.26_Mobile.29) - "Coders (Web & Mobile)" section.

Thank you very much [to all contributors](https://github.com/HabitRPG/habitrpg-android/graphs/contributors).

#### Steps for contributing to this repository:

1. Fork it
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Create new Pull Request
   * Don't forget to include your Habitica User ID, so that we can count your contribution towards your contributor tier

### Code Style Guidelines
We follow the code style guidelines outlined in [Android Code Style Guidelines for Contributors](https://source.android.com/source/code-style.html).

You can install our code style scheme to Intellij and/or Android Studio via this shell command:

    $ ./install-codestyle.sh

## Build Instructions

### Config Files

Setup Habitica build config files by simply copying the example habitica files.

    $ cp habitica.properties.example habitica.properties
    $ cp habitica.resources.example habitica.resources
    $ cp Habitica/google-services.json.example Habitica/google-services.json (Get .json from Firebase Console)

Note: this is the default production `habitica.properties` file for habitica.com. If you
want to use a local habitica server, please modify the values in the properties file accordingly.

Building also requires a google-services.json file:  Register/Login to Firebase, create new project called Habitica, create two apps, com.habitrpg.android.habitica and com.habitrpg.android.habitica.debug.
Download google-services.json, copy to Habitica/google-services.json.
