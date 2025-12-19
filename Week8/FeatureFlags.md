# Resources
[Youtube](https://www.youtube.com/watch?v=AJa2B-twtG4&t=20s)

# Overview
Feature flags (also known as feature toggles) are a powerful technique in software development that allows developers to enable or disable specific features or functionalities in an application without deploying new code. 
This approach provides flexibility, allowing teams to test new features, perform A/B testing, and manage releases more effectively.


## Pros
1. can turn on/off features without redeploying code
2. test new features in production with real users
3. segment users based on criteria (location, subscription level, etc.)
4. can automate with time-based or event-based triggers
   - e.g. feature is enabled for 3 weeks, then automatically disabled


## How to Implement
1. JSON configuration file properties
2. Feature flag management services (LaunchDarkly, Firebase Remote Config, etc.) -> Better for large scale applications
   - centralized management of feature flags
   - can turn on/off features remotely without modifying app code
   - get audit and usage analytics


## Example
- We have an Ice cream app that has a new promotion banner -> we only want to show it when users are close to the ice cream shop location

```kotlin
// if store_open feature flag is enabled
// then show open store banner

// now segments
// check for current user location and zipcode
// Segment A will be for some users in a specific location
// Segment B will be for internal testing of employees -> use email id
    // lets say something went wrong with Segment B -> can turn it off 
    // fix the issue and turn it back on -> once satisfied, can turn on for Segment A (real users)
```

## How to Create a Feature in our App
1. Define the feature flag in a configuration file or feature flag management service
2. Implement logic in the app to check the status of the feature flag
3. Use conditional statements to enable or disable the feature based on the flag's status
4. Test the feature flag implementation in different scenarios (enabled, disabled, segmented users)
5. Deploy the app with the feature flag in place
6. Monitor the feature's performance and user feedback
7. Gradually roll out the feature to more users or segments as needed
8. Once the feature is stable and well-received, consider removing the feature flag to clean up the codebase

```kotlin
// Pseudo code example
@Composable
fun IceCreamApp() {
    val isStoreOpenFeatureEnabled = FeatureFlagManager.isFeatureEnabled("store_open_banner")
    if (isStoreOpenFeatureEnabled) {
        StoreOpenBanner()
    }
    // other app content
}
@Composable
fun StoreOpenBanner() {
    // UI for the store open banner
}
```