## Develop an Android app that enables you to track your walk with images 👷‍♂️
The user opens the app and presses the start button.  
After that the user puts their phone into their pocket and starts walking.  
The app requests a photo from the public flickr photo search api for his location every 100 meters to add to the stream.  
New pictures are added on top. Whenever the user takes a look at their phone,  
they see the most recent picture and can scroll through a stream of pictures which shows where the user has been.    
It should work for at least a two-hour walk. The user interface should be simple as shown on the left of this page.  

## Built With 🛠
- [Kotlin](https://kotlinlang.org/) - Modern programming language for Android development.
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture) - Collection of libraries that help you design robust, testable, and maintainable apps.
    - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Stores UI-related data that isn't destroyed on UI changes.
    - [Room](https://developer.android.com/topic/libraries/architecture/room) - SQLite object mapping library.
    - [Navigation](https://developer.android.com/guide/navigation/navigation-principles) - A bit over the top for this single screen app, but I like how easy it is possible to add other screens with this library.
- [Retrofit](https://square.github.io/retrofit/) - A type-safe HTTP client for Android and Java.
- [OkHttp](http://square.github.io/okhttp/) - HTTP client that's efficient by default: HTTP/2 support allows all requests to the same host to share a socket
- [Gson](https://github.com/google/gson) - used to convert Java Objects into their JSON representation and vice versa.
- [Coroutines](https://developer.android.com/kotlin/coroutines) - Lightweight framework that simplifies multithreading on Android.
- [Livedata](https://developer.android.com/topic/libraries/architecture/livedata) - An observable data holder class.
- [Coil](https://coil-kt.github.io/coil/) - An image loading library for Android backed by Kotlin Coroutines.
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) - Hilt is a dependency injection library for Android that reduces boilerplate.
- [DataBinding](https://developer.android.com/topic/libraries/data-binding) - The Data Binding Library is a support library that allows you to bind UI components in your layouts to data sources in your app using a declarative format rather than programmatically.
- [Mockk](https://mockk.io) - Mocking library for Kotlin.
- [Espresso](https://developer.android.com/training/testing/espresso) - For Android UI tests.

## Architecture

I chose an MVVM architecture with the following layers:

### Layers
- **Domain** - Would execute business logic which is independent of any layer.
- **Data** - Where the data from remote or cached source is handled.
- **Presentation** - The role of the presentation layer is to display the application data on the screen.

### Disclaimer - Considering multi modules 🗄
We're all used to monolithic Android applications, where all your classes are inside one module divided into multiple packages.
Unfortunately, this approach tends to lead to high build times, difficulties to manage code in large teams and impossibility to reuse code across apps.

Although we're not gonna take this approach here as this app is really simple, there are useful resources on the subject:
- [Modularizing Android Applications](https://medium.com/google-developer-experts/modularizing-android-applications-9e2d18f244a0) - By Joe Birch.
- [Create an Android Library](https://developer.android.com/studio/projects/android-library.html) - Android Documentation.

### Final approach
For the simplicity of things we'll create a monolithic with two different packages: **core** and **features**.
**core** contains all the common/reusable code and **features** contains two features: **photos** and **tracking**.  

<center><img width="300" height="200" src="https://developer.android.com/topic/libraries/architecture/images/mad-arch-overview.png"><p>- photo by: <a href="https://developer.android.com/jetpack/guide">Android Guide to Architecture</a></p></center>

> Nb: In the illustration above the UI layer would be our presentation layer

That is for our global vision. Now if we want a more refined one, this graph would be suitable:

<center><img width="500" height="400" src="https://miro.medium.com/max/1400/1*-yY0l4XD3kLcZz0rO1sfRA.png"><p>- photo by: <a href="https://developer.android.com/jetpack/guide">Android Guide to Architecture</a></p></center>

Let's take each element one by one and see how it links to this project:
- **Activity/Fragment** - This is our *MainActivity* hosting our *PhotosFragment*. (presentation layer)
- **ViewModel** - *PhotosFragment*'s *PhotosViewModel* observes a stream of photos. (presentation layer)
- **Repository** - *PhotoRepository* interface with *searchPhotoForLocation(lat: String, lon: String)*. (domain layer)
- **Model** - This is our Room database (*PhotoDatabase* with *PhotoEntity* objects). (data layer)
- **Remote data source** - *PhotoService* which call the Flickr API. (data layer)

## Challenges

### Location tracking

For this purpose you need to perform location updates in the background. A service seems to be a good solution:  

> A Service is an application component that can perform long-running operations in the background  

Then the other thing to figure out is to choose which service from three different types: *Background*, *Foreground* and *Bound*.  
Each of these terms are misleading because it is not describing the behavior of how each service are used,  
but it is describing how they are terminated.  

- A Background Service is a service that runs only when the app is running so it’ll get terminated when the app is terminated.  
- A Foreground Service is a service that stays alive even when the app is terminated.  
- And a Bound Service is a service that runs only if the component it is bound to is still active.  

I decided to opt for a foreground service as I have more guarantees that the location will be tracked even though the app is killed.  
Plus, we can associate a notification to it to notify the user that his location is being tracked.

### Testing

Testing location tracking can seem difficult. But thankfully you can trace a route via your Android emulator.  
We can just start tracking and trigger the route at the same time to simulate a hike.

## Improvements

[Compose](https://developer.android.com/jetpack/compose) would have been welcome. I'm not 100% fluent on it so i didn't take risk of running into any hiccups.  

Keep track of your different walks, and trace them on a map view with polylines.

