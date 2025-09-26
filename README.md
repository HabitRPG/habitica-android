# Habitica for Android

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

If you are deploying the companion self-hosted server (`https://github.com/sudoxnym/habitica-self-host`), use the APKs named below so the login screen can target that backend out of the box.

Self-hosted builds published from this repository include two APKs in each release:

- `habitica-self-host-debug.apk` – debuggable build with developer tooling enabled
- `habitica-self-host-release.apk` – optimized release build ready for deployment

If you Watch this repository, GitHub will send you an email every time we publish an update.

## Contributing

Thank you very much [to all contributors](https://github.com/HabitRPG/habitrpg-android/graphs/contributors).

#### How mobile releases work

All major mobile releases are organized by Milestones labeled with the release number. The 'Help Wanted' is added to any issue we feel would be okay for a contributor to work on, so look for that tag first! We do our best to answer any questions contributors may have regarding issues marked with that tag. If an issue does not have the 'Help Wanted' tag, that means staff will handle it when we have the availability. 

The mobile team consists of one developer and one designer for both Android and iOS. Because of this, we switch back and forth for releases. While we work on one platform, the other will be put on hold. This may result in a wait time for PRs to be reviewed or questions to be answered. Any PRs submitted while we're working on a different platform will be assigned to the next Milestone and we will review it when we come back!

Given that our team is stretched pretty thin, it can be difficult for us to take an active role in helping to troubleshoot how to fix issues, but we always do our best to help as much as possible :) With this in mind, when selecting issues to work on it may be best to pick up issues you already have a good idea how to handle and test. Thank you for putting in your time to help make Habitica the best it can be!

#### Steps for contributing to this repository:

1. Fork it
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Create new Pull Request
   * Don't forget to include your Habitica User ID, so that we can count your contribution towards your contributor tier

### Code Style Guidelines
We use Kotlin and follow the code style based on the [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide).

## Build Instructions

### Config Files

1. Setup Habitica build config files by simply copying or renaming the example habitica files:

   `habitica.properties.example` to `habitica.properties`

   `habitica.resources.example` to `habitica.resources`

   You also need `google-services.json`. Download it from Firebase in the next step.


   Note: this is the default production `habitica.properties` file for habitica.com. If you want to use a local Habitica server, please modify the values in the properties file accordingly.

   When running a self-hosted build you can now switch servers directly from the login screen—ideal for pairing with [`habitica-self-host`](https://github.com/sudoxnym/habitica-self-host). Tap the gear icon in the upper-right corner seven times to unlock the developer options dialog, enter your custom base URL, and (optionally) enable UnifiedPush with the distributor installed on the device.




2. Go to https://console.firebase.google.com

   a. Register/Login to Firebase. (You can use a Google account.)

   b. Create a new project called Habitica

   c. Create two apps in the project: `com.habitrpg.android.habitica` and `com.habitrpg.android.habitica.debug`

   d. Creating each app will generate a `google-services.json` file. Download the `google-services.json` file from the second app and put it in `\Habitica\` and `\wearos\`

   You can skip the last part of the app creation wizards (where you run the app to verify installation).



3. If using Android Studio, click Sync Project with Gradle Files. Update Android Studio if it asks you to update. Run Habitica.
