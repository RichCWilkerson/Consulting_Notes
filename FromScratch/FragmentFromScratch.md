# Create a new Fragment from Scratch
## AndroidManifest.xml
- don't need to declare fragments here. Only activities need to be declared.
 
--- 

## Navigation Options
- link to navigation notes to decide best practices for your use case.
  - UI options, setup, pros/cons, etc.
- [Navigation Setup Notes](navigationOptions.md)


- add a new destination in the navigation graph XML file.
    - Start here when creating a new screen, click the + button to add a new fragment.
        - edit package name to match your directory structure.
            - /ui/name_of_feature -> creates a new class in that directory and places the layout file in res/layout.
        - next create links between fragments by dragging or right clicking and selecting "Add Action".
            - take these actions and use them in your button's onClickListener to navigate.
- If you set a toolbar menu or drawer menu, NavController can automatically handle navigation for you.
    - just need to setup the menu item ids to match the fragment ids in the nav graph.
    - don't need to create actions for these menu items. because NavController will handle it automatically.
    - example: drawer_menu.xml has an item with id "nav_home" that links to HomeFragment in nav_graph.xml
        - NavController will handle the navigation when that menu item is clicked.
    - setup in MainActivity.kt with:
        - appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout)
            - AppBarConfiguration tells the NavController which destinations are top-level (no "Up" button) and what the drawer layout is.
        - setupActionBarWithNavController(navController, appBarConfiguration)
            - setup allows the action bar (toolbar) to automatically handle navigation and display the correct title.
        - navView.setupWithNavController(navController)
            - setup allows the navigation view (sidebar) to automatically handle navigation when menu items are clicked.
            - navView is the NavigationView from activity_main.xml

### Navigation UI Options
- If you want to create a Bar, Menu, or Sidebar for navigation
- link to NavigationUI notes to decide best practices for your use case.
- [NavigationUI Setup Notes](./navigationUIOptions.md)

---

## Layout (res/layout)
- create a new XML layout file for the fragment's UI.
- use a naming convention like fragment_your_fragment_name.xml







- always create the bar in the main activity layout (activity_main.xml)
    - this way it is persistent across all fragments.
    - if you want to have two different bars for different sections of the app, you can create two different activities.
        - each activity can have its own bar and set of fragments.

- define menu items with ids that match fragment ids in the nav graph for automatic navigation handling

## Fragment Kotlin file
- create a new Kotlin class that extends Fragment.
  - gives access to fragment lifecycle methods and features.

### Fragment Lifecycle (onAttach, onCreate, onCreateView, onViewCreated, onStart, onResume, onPause, onStop, onDestroyView, onDestroy, onDetach)
- link to lifecycle notes to decide best practices for your use case.
- [Fragment Lifecycle Notes](./fragmentLifecycle.md)

### Setup viewWiring (recommend viewBinding)
- link to viewWiring notes to decide best practices for your use case.
- [ViewWiring Setup Notes](viewWiringOptions.md)

--- 

## ViewModel.kt Setup (persisting UI data and state)
- link to VM notes to decide best practices for your use case.
- [ViewModel Setup Notes](viewModelOptions.md)




- for Top App Bar:
  - use a Toolbar inside an AppBarLayout.
  - set the Toolbar as the ActionBar in MainActivity.kt with setSupportActionBar().
- for Bottom Navigation Bar:
  - use a BottomNavigationView.
  - setup with NavController in MainActivity.kt with navView.setupWithNavController(navController).