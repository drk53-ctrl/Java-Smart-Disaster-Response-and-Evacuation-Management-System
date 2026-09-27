# Smart Disaster Response System (SDRS) — Viva & Presentation Guide

Everything in this document describes what is ACTUALLY in the code. Read it once
before your review and you will be able to answer questions about any module.

---

## A. Project Overview

- **Project name:** Smart Disaster Response System (Disaster Evacuation Management System)
- **Problem it solves:** During a disaster (flood, cyclone, earthquake, fire),
  response teams must quickly answer: *who* is affected, *where* are they,
  *which* shelter can take them, and *which route* reaches it fastest. Doing
  this with paper registers and phone calls is slow and error-prone.
- **Why it is needed:** It puts citizens, disasters, shelters, teams, vehicles,
  resources and the road network into ONE system, and automatically computes
  priorities and best routes.
- **Main objective:** A single desktop application where an operator can
  register citizens, report disasters, manage shelters/resources, evacuate
  people by priority, and visually plan emergency routes on an interactive map.

---

## B. Technologies Used (and WHY)

| Technology | What it is here | Why it was chosen |
|---|---|---|
| Java (JDK 8+, runs on 24) | The whole language | Platform-independent, strong OOP, standard for college DS/algorithm work |
| Java Swing | All GUI (`javax.swing`, `java.awt`) | Built into the JDK — zero external dependencies, works offline, easy to demo |
| Java 2D (custom painting) | `MapCanvas` draws the map with `Graphics2D` | Lets us draw our own simulated map (nodes, roads, routes) with no map API |
| Java Serialization | `ObjectOutputStream` / `ObjectInputStream` | Persists the whole database as one file with almost no code — no DB install needed |
| Dijkstra's algorithm | `RouteFinder` | Classic single-source shortest path for weighted (non-negative) road networks |
| Graph + HashMap | `Graph`, adjacency lists | Roads are naturally graphs; HashMap gives O(1) node lookup |
| No external libraries | `compile.bat` compiles with plain `javac` | Nothing to install or download — runs on any machine with a JDK |

---

## C. Main Modules (all exist, all wired together)

1. **Dashboard** — 12 live stat cards (citizens, active/critical disasters,
   shelters, occupancy, teams, vehicles, resources), priority-ranked critical
   alerts (from the max-heap), recent disasters.
2. **Citizen Management** — register/edit/delete/search/filter citizens;
   duplicate-phone and 10-digit-phone validation; each citizen has a computed
   **priority score** (age, health, location risk) used by evacuation.
3. **Disaster Management** — report disasters (flood/cyclone/earthquake/fire/
   earthquake-aftermath types), attach affected citizens, change status
   (Reported → Active → Contained → Resolved; cannot resolve while evacuations
   are pending), "Sort by Priority" triage view (heap).
4. **Evacuation Management** — create evacuation records (citizen + disaster +
   people count), manual shelter assignment or **auto-assign**: the system
   picks a reachable shelter with free capacity, best by priority, using a
   JDK PriorityQueue of pending evacuations.
5. **Shelter Management** — shelters with capacity, live occupancy bars,
   status (Open/Full/Closed), hard capacity enforcement, evacuee list per shelter.
6. **Emergency Teams** — medical/fire-rescue/search-rescue/police units,
   availability state, assign/release to disasters.
7. **Resource Management** — stock (water, food, medical kits, blankets, fuel…),
   allocation to disasters with **over-allocation protection**, allocation
   history, and a **greedy allocation plan** (urgent disasters first).
8. **Vehicle Management** — ambulances, fire trucks, rescue/supply vehicles,
   assign/release to disasters.
9. **Emergency Navigation (the Map)** — the simulated interactive map (details
   in Part E of this guide and section 6/7 below).
10. **Route Planning** — road-network editor: add locations/roads, block or
    delete roads (with BFS disconnection warning), find best route
    (distance or time mode), alternative routes (DFS), nearest shelters,
    blocked-road impact analysis (BFS), plus a Map View tab.
11. **Response Center** — pick ONE disaster and see its whole coordination
    chain: affected citizens ranked by priority, assigned teams/vehicles,
    recommended resources, evacuations, shelters, a recommended route from the
    emergency center, and a FIFO dispatch queue.
12. **User Management** — user records (id, username, full name, phone, role)
    with add / reset-password / delete (last-admin protection). These are
    records only — nothing in the app requires logging in.

---

## D. How To Use The Application (demonstration flow)

1. Start the app (`run.bat`, or `java -cp out sdrs.Main`). **The Dashboard opens
   directly — there is no login.** Point out the stat cards and critical alerts.
2. **Citizen Management → Add Citizen.** Try an invalid phone ("123") to show
   validation, then add a real one. Click a row → View Details → show the
   computed priority score.
3. **Disaster Management → Report Disaster.** Choose type Flood, location
   "Riverside Colony" (a Disaster Zone node), severity Critical. It appears in
   the table with status "Active".
4. **Emergency Navigation (map).** The disaster zone now shows a red
   "ACTIVE DISASTER" badge. Click circles to inspect locations (shelters show
   capacity/occupied/available; disasters show severity and affected count).
5. On the map: set **Source = Riverside Colony**, **Destination = City General
   Hospital**, click **FIND ROUTE**. The yellow highlighted path appears on the
   map with per-leg "km, min" labels; the result panel shows the full route,
   total distance and estimated time. Click **Nearest Shelter** to let the
   system choose the best shelter automatically.
6. **Route Planning.** Show the road table, block "Hospital Road" (confirm the
   BFS warning if it would disconnect anything), then re-run the route — the
   route changes and the blocked road is drawn dashed red. Use "Alternative
   Routes (DFS)" to show options.
7. **Evacuation Management.** Create an evacuation for a critical citizen of
   the disaster, then **Auto-Assign Shelters** — the system chooses a shelter
   with free capacity. Move the record through Requested → Evacuating →
   Evacuated; watch shelter occupancy rise on the Shelters screen and the map.
8. **Shelters** — show occupancy bars and capacity enforcement (try to exceed).
9. **Resources** — allocate stock to the disaster (try over-allocating to see
   the guard), then show the greedy plan.
10. **Response Center** — select the disaster; walk the audience through the
    entire coordination chain on one screen.
11. Back to **Dashboard** — numbers updated everywhere. Close the app
    (Exit button or window X) and reopen: **all data is still there**
    (persistence demo).

---

## E. Algorithms & Data Structures (only ones actually in the code)

| Structure / algorithm | Where (exact class) | Why | How it works here |
|---|---|---|---|
| **Dijkstra's algorithm** | `algorithm/RouteFinder`, called by `RouteService` | Shortest path in a weighted graph with non-negative weights — exactly what road km/minutes are | Builds the graph from `RoadSegment`s (weight = km or minutes depending on mode), greedily settles the closest unsettled node until the destination is reached; the parent chain becomes the route |
| **Graph + adjacency list** | `algorithm/Graph` (`HashMap<String, List<RoadSegment>>`) | Road networks ARE graphs; adjacency lists make neighbor scans cheap and support removing/blocked edges | Locations = nodes, roads = edges; blocked roads are skipped during pathfinding |
| **Binary max-heap (hand-written)** | `algorithm/DisasterPriorityQueue` | O(log n) access to the most urgent disaster for triage | Priority score = severity weight + affected people; drives "Sort by Priority" and dashboard alerts |
| **JDK PriorityQueue (min-heap)** | `EvacuationService` | Evacuations must be processed most-urgent-first | Pending evacuations ordered by citizen priority; auto-assign pops in that order |
| **BFS** | `NetworkService.criticalRoadsFor`, `RouteService.blockedRoadImpact` | Reachability on unweighted edges — "if I block this road, what becomes unreachable?" | Level-by-level traversal over open roads only |
| **DFS with backtracking** | `RouteService.alternativeRoutes` | Enumerate distinct alternative routes beyond the optimal one | Depth-first path enumeration, ranked by total cost |
| **Greedy allocation** | `ResourceService` | Allocate limited stock to the most urgent disasters first | Sort disasters by priority, fill until stock runs out, report unmet demand |
| **Merge sort (hand-written)** | `algorithm/SortSearch` | Stable O(n log n) sort for table ranking | Used for priority/display sorting |
| **Binary search (hand-written)** | `algorithm/SortSearch` | O(log n) lookup in sorted ID lists | Used where lists are already sorted |
| **HashMap indexes** | `DataManager` (7 index maps) | O(1) lookup of any record by ID | Rebuilt on load, maintained on add/remove |
| **FIFO queue** | `DataManager.taskQueue` (LinkedList) | Dispatch tasks processed in arrival order | Response Center dispatch queue |
| **LIFO stack** | `DataManager.deletedStack` | Undo of last delete | Push deleted entity, pop to restore |
| **ArrayList** | everywhere in `SystemData` | Simple ordered storage of records | The serializable "database tables" |

**Dijkstra on the map, concretely:** the map screen asks for Source and
Destination. `RouteService.findRoute(from, to, timeMode)` runs Dijkstra over
the same `Location`/`RoadSegment` data the map draws. The returned
`RouteResult` contains the node path + per-leg km/minutes; `MapCanvas` paints
that exact path as a thick yellow line, and the panel prints
"Riverside Colony → Highway Junction → … → City General Hospital" with totals.

---

## F. Data Flow

```
User input (button click / form)
      ↓
UI panel or dialog  (sdrs.ui.*, e.g. CitizensPanel + CitizenDialog)
      ↓
Service layer       (sdrs.service.*, e.g. CitizenService — validates, applies rules)
      ↓
DataManager         (updates the SystemData lists + HashMap indexes, notifies listeners)
      ↓
FileDataStore.save  (whole SystemData serialized to data/sdrs_data.ser, .bak backup)
      ↓
notifyChanged()     → all panels refresh → UI shows the new state
```

Example: "Add Citizen" → `CitizenService.addCitizen(...)` validates phone/age →
`DataManager.addCitizen(...)` + index update → `notifyChanged()` → file saved →
table refreshes everywhere (dashboard count included).

For the map: click → `MapCanvas.hitTest` → `RouteService` reads the same
in-memory `SystemData` graph → Dijkstra computes the route → `MapCanvas`
repaints with the highlighted path.

---

## G. Project Architecture (actual classes)

```
sdrs.Main                    → starts the app: installs the theme, opens MainFrame
sdrs.ui.MainFrame            → main window: sidebar (12 modules) + CardLayout content
sdrs.ui.MapCanvas            → custom-painted Swing map (nodes, roads, zoom, click-select)
sdrs.ui.panels.*             → one panel per module (12 panels)
sdrs.ui.dialogs.*            → modal add/edit dialogs (7 dialogs)
sdrs.ui.Theme / UiUtil       → colors/fonts + shared UI helpers (tables, cards, forms)
sdrs.model.*                 → Citizen, Disaster, Shelter, EmergencyTeam, Resource,
                               Vehicle, Evacuation, Location (map x/y), RoadSegment,
                               Allocation, User, Person (abstract), Prioritizable (interface)
sdrs.algorithm.*             → Graph, RouteFinder (Dijkstra), RouteResult, RouteStep,
                               DisasterPriorityQueue (heap), SortSearch
sdrs.service.*               → one service per module + DataManager + AppContext (wiring)
sdrs.data.*                  → SystemData (serializable aggregate), DataStore interface,
                               FileDataStore (file + .bak, corrupt-file recovery)
sdrs.util.*                  → Validators, AppException, IdGenerator, PasswordUtil (SHA-256),
                               SeedData (first-run demo data)
sdrs.test.*                  → TestHarness (45 backend tests), GUISmokeTest (21 UI tests),
                               LayoutOverlapTest (overlap audit)
```

Layering rule: **UI → Services → DataManager → DataStore.** The UI never
touches files; services validate everything; algorithms are called from
services and their results drive what the UI shows.

---

## H. Important Viva Questions & Answers

**Q: Why did you choose Java?**
A: Platform independence (runs on any machine with a JVM), strong OOP support
for modeling entities, and rich standard libraries for GUI (Swing) and file
handling — with zero external dependencies.

**Q: Why Swing and not JavaFX or a web app?**
A: Swing is built into the JDK, works completely offline, needs no extra
installation on the demo machine, and is fully sufficient for a desktop
management tool. A web app would need a server; this project is a desktop system.

**Q: How does the map work? Is it Google Maps?**
A: No external map service. It is a **simulated map** drawn with Java 2D
(`MapCanvas.paintComponent`). Locations are stored with (x, y) coordinates in
the graph; the canvas converts them to screen positions, draws roads as lines
between nodes, colors nodes by type, and draws the computed route as a yellow path.

**Q: How does route finding work?**
A: The road network is a weighted graph. Dijkstra's algorithm (`RouteFinder`)
finds the minimum-cost path between source and destination, where cost is
distance (km) or time (minutes) depending on the selected mode. Blocked roads
are excluded. The resulting node path is shown as text AND highlighted on the map.

**Q: Why Dijkstra?**
A: Road distances/times are non-negative weights, which is exactly
Dijkstra's precondition; it guarantees the optimal path. BFS would only work
for unweighted graphs (we use BFS separately for reachability checks), and
DFS doesn't guarantee shortest paths (we use DFS only to enumerate alternatives).

**Q: Where is the data stored? Is it temporary or permanent?**
A: **Permanent.** Every change is serialized to `data/sdrs_data.ser` (a binary
file next to the application), with a `.bak` backup of the previous state. It
is NOT just ArrayLists in memory — memory holds the working copy; the file is
the durable store.

**Q: What happens when the application is closed and reopened?**
A: On close, data is saved (also saved after every operation). On the next
start, `DataManager` loads the file; if it is missing, realistic demo data is
created; if it is corrupt, the app recovers (falls back to seed data; a
`.bak` copy also exists).

**Q: What happens if two disasters are registered?**
A: Each gets a unique ID (DIS-1, DIS-2…), both appear in the table, and both
are considered by the priority heap — the dashboard and triage view rank them
by severity and affected people.

**Q: How are citizens associated with disasters?**
A: A `Disaster` holds a list of citizen IDs (`getCitizenIds()`). Reporting a
disaster can attach citizens; evacuations reference both citizen and disaster.

**Q: Which data structures did you use and why?**
A: ArrayList for record storage (simple, ordered), HashMap for O(1) ID lookup
indexes, adjacency-list Graph for roads, two heaps (hand-written max-heap for
disaster triage, PriorityQueue for evacuation order), FIFO queue for dispatch,
LIFO stack for undo. Each solves a specific problem — nothing is decorative.

**Q: What are the limitations?**
A: Single-user desktop app (no network/multi-operator sync); the map is
schematic (logical coordinates, not real GIS); serialization instead of a
SQL database (fine for this scale, harder to query ad hoc); Swing look varies
slightly across OS themes.

**Q: What can be improved in the future?**
A: Switch storage to SQLite/MySQL; add real map tiles or GPS data; multi-user
with roles and login over a network; live disaster feeds; export reports to
PDF/Excel; pathfinding that avoids disaster-zone nodes automatically.

**Q: What is the role of the main classes?**
A: See section G — Main (entry), MainFrame (window+navigation), panels
(screens), services (business rules), DataManager (data + indexes + save),
FileDataStore (file I/O), model classes (entities), algorithm classes
(Dijkstra, heap, sorting).

**Q: How is the priority score computed for citizens?**
A: From age band, medical notes and location risk (citizens at an active
disaster zone rank higher). It is computed in the Citizen model and displayed
in the detail view; evacuation auto-assign uses this priority.

**Q: What testing did you do?**
A: 45 backend tests (`TestHarness`) covering algorithms, map/graph
integration, a full end-to-end workflow, validation and persistence; 21 UI
tests (`GUISmokeTest`) that construct every screen and resize the window at
three sizes; an automated overlap audit (`LayoutOverlapTest`).
