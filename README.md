# 🐱 Cats API Sample Project

A modern Android app showcasing clean architecture principles using Jetpack Compose, Kotlin Coroutines, Flow, Hilt, and MVVM. It fetches and caches data about cat breeds from [TheCatAPI](https://thecatapi.com), allowing users to explore different breeds, view images, and mark their favorites — even offline.

---

## 🚀 Features

- 📡 **Remote & Local Data Sources**  
  - Syncs from the network, caches to Room DB.
  - Handles offline data gracefully.

- 💾 **Favorites with Offline Queueing**  
  - Favourites are associated via image IDs.
  - Pending operations are synced when connectivity is restored.

- 🧭 **Jetpack Navigation**  
  - Bottom navigation between breed list and favorites.
  - Splash screen initializes and routes user appropriately.

- 🧪 **Test Coverage** 
  - ViewModels, Use Cases, Repository, local DB and Mappers tested with `Turbine`, `MockK`, and `JUnit`.

- 📦 **Architecture**
  - MVVM + Clean Architecture.
  - Separated `dataLayer`, `domain`, and `ui`.

---

## 🧱 Tech Stack

| Layer        | Techs Used                                 |
|--------------|--------------------------------------------|
| UI           | Jetpack Compose, Navigation, Material3     |
| DI           | Hilt                                       |
| Data         | Retrofit, Room, TheCatAPI, Coroutine Flow  |
| Utilities    | Resource Wrapper, Error Handling, Mappers  |
| Testing      | JUnit4, MockK, Turbine, Coroutine Test     |

---

## 📷 Screenshots

<img src="https://github.com/user-attachments/assets/fdb5111a-130e-4d14-a950-44e74ab4f689" width=20% height=20%>

<img src="https://github.com/user-attachments/assets/a673305e-dd3e-4c30-b97a-9976dc31c467" width=20% height=20%>

<img src="https://github.com/user-attachments/assets/2109fc58-49b9-4d94-bab9-2a160641ca60" width=20% height=20%>

<img src="https://github.com/user-attachments/assets/bf14430f-6eb8-4f48-a621-25580d33d428" width=20% height=20%>

---

## 💡 Project Structure

```
com.example.catsapisampleproject/
│
├── dataLayer/          # DTOs, Room entities, repository impl
├── domain/             # UseCases, models, and mappers
├── ui/                 # Compose UI, ViewModels, Navigation
├── util/               # Resource wrappers, error types, etc.
└── di/                 # Hilt module injections
```

---

## 🔄 Offline Support

- All data is cached in Room.
- Favorites are updated and queued when offline.
- Queued operations are retried when the app regains network connectivity.

---

## 🛠️ Setup Instructions

   Add your API Key for [TheCatAPI](https://thecatapi.com) in your `local.properties`:
   ```properties
   CAT_API_KEY=your_api_key_here
   ```

### 🧠 Design Considerations & Assumptions

- **Search Endpoint Limitations**  
  During development, I identified that the endpoint used to filter cat breeds by name **did not support pagination**, unlike the main paginated listing. As such, I chose to fetch and filter data **locally** after the initial load. This decision ensures responsiveness while respecting API limitations.  
  I opted for this solution particularly because this feature focuses on **breed listing**, which is a **limited and known subset**, making it feasible and efficient to **fully cache** locally.

- **Favoriting Architecture**  
  Since the favourite actions are tied to the **image ID**, breeds without an associated `referenceImageId` were treated as **non-favouritable**. The UI and logic gracefully skip these breeds without error. This constraint was communicated clearly through mapping and fallback strategies.

- **Offline Support & Resilience**  
  Recognizing that network requests may fail or be delayed, the app caches data locally using Room. If an operation like favoriting fails due to connectivity issues, the action is **queued locally** with a `PendingOperation` flag. Once the app regains connectivity, these are synced automatically with the backend.

- **Reactive Data Flow**  
  All core use cases expose their data through **`Flow<Resource<T>>`**, supporting Loading, Success, and Error states. This provides composables with real-time updates and aligns well with Jetpack Compose’s reactive paradigm.

- **Single Source of Truth**  
  The local Room database acts as the single source of truth. Even data received from the remote API is first cached before being exposed to the UI. This approach promotes consistency, offline capabilities, and easier testing.

### 🛠️ Limitations

- While the project features a strong separation of concerns and supports reactive state management via Kotlin Flows, testing remains a work in progress. I'm currently expanding my understanding of best practices for testing modern components such as Flow, StateFlow, and handling asynchronous data streams (e.g., with Turbine). Some test coverage is present and functioning correctly, but further robustness, especially for edge cases and error propagation, is something I'm actively improving.
