# Smart Disaster Response System (SDRS)

A complete Java Swing desktop application for disaster-evacuation management,
built with core Java only (no external libraries). It demonstrates OOP design,
data structures and algorithms, file persistence, validation and a polished
multi-module GUI.

## How to Run

Windows:
1. Double-click `compile.bat` (or run it from a terminal).
2. Double-click `run.bat`.

Any OS with JDK 8+:
```
javac -d out $(find src -name "*.java")
java -cp out sdrs.Main
```

Automated tests:
```
java -cp out sdrs.test.TestHarness      45 backend tests (map/graph integration, workflow, algorithms, validation, persistence)
java -cp out sdrs.test.GUISmokeTest     21 UI tests (all 12 screens, layout at 3 window sizes, map canvas painting)
java -cp out sdrs.test.LayoutOverlapTest  automated overlap audit: sibling component bounds checked
                                         across all 12 screens at 3 window sizes + sidebar width check
```

The application opens directly into the Dashboard. There is no login screen;
all 12 modules are available from the sidebar. User accounts (for the User
Management module records) are seeded with SHA-256-hashed passwords.

## Module Map

| Sidebar item | What you can do |
|---|---|
| Dashboard | 12 live stat cards, priority-ranked critical alerts, recent disasters |
| Citizens | Register/search/filter/edit/delete citizens, detail view with priority score |
| Disasters | Report/search/filter disasters, change status, priority-sorted triage view |
| Evacuation | Create evacuation records, assign shelters manually or auto-assign by priority |
| Shelters | Manage shelters, capacity enforcement, occupancy bars, evacuee lists |
| Emergency Teams | Teams with availability, assign/release to disasters |
| Resources | Stock management, allocation with over-allocation protection, greedy plan |
| Vehicles | Vehicles with assignment state, assign/release to disasters |
| Emergency Map | Simulated interactive operations map wired to the road graph: colored/glyph location nodes, weighted road edges, blocked-road dashes, live disaster and shelter badges, click-for-details panel, source/dest tags, Dijkstra route highlighted in yellow |
| Route Planning | Road network editor, Dijkstra shortest/fastest route, DFS alternatives, BFS impact, nearest-shelter greedy search, plus a Map View tab sharing the same canvas and graph |
| Response Center | One disaster, full coordination chain and actions in one screen |
| User Management | Admin-only: create users, reset passwords, delete users |

## Architecture

```
src/sdrs
├── Main.java                entry point
├── model/                   Citizen, Disaster, Shelter, EmergencyTeam, Resource,
│                            Vehicle, Evacuation, Location, RoadSegment, User,
│                            Allocation, Person (abstract), Prioritizable (interface)
├── algorithm/               Graph, RouteFinder (Dijkstra), DisasterPriorityQueue
│                            (hand-written max-heap), SortSearch (merge sort,
│                            binary search)
├── service/                 DataManager (indexes, listeners, save),
│                            CitizenService, DisasterService, ShelterService,
│                            EvacuationService, TeamService, VehicleService,
│                            ResourceService, NetworkService, RouteService,
│                            ResponseService, AppContext
├── data/                    SystemData (serializable aggregate), DataStore
│                            (interface), FileDataStore (file + .bak backup)
├── util/                    Validators, AppException, IdGenerator, PasswordUtil,
│                            SeedData
├── ui/                      MainFrame, MapCanvas, Theme, UiUtil, StatCard,
│                            Refreshable, WrapLayout
│   ├── panels/              one panel per module (11 panels)
│   └── dialogs/             one modal dialog per entity (7 dialogs)
└── test/                    TestHarness, GUISmokeTest
```

Layering: UI -> Services -> DataManager -> DataStore. The UI never touches
files directly; services validate everything; algorithms are called from
services and drive visible UI results.

## Algorithms and Data Structures (and why)

| Structure / algorithm | Where it is used | Why it fits |
|---|---|---|
| Binary max-heap (`DisasterPriorityQueue`, hand-written) | Disaster triage ranking, dashboard critical alerts | O(log n) extraction of the most urgent disaster; heap order drives the ranking shown in the UI |
| Graph (adjacency list `HashMap<String, List<RoadSegment>>`) | Road network, blocked-road support | Road networks are graphs; adjacency lists give cheap neighbor scans |
| Dijkstra (`RouteFinder`) | Route Planning, Emergency Map route highlight, Response Center recommended route, nearest-shelter search | Non-negative weights (km/minutes) satisfy Dijkstra's precondition |
| BFS | Blocked-road impact analysis, connectivity checks | Unweighted reachability is exactly a BFS problem |
| DFS with backtracking | Alternative route enumeration | Exhaustive simple-path exploration when the best route is unusable |
| `java.util.PriorityQueue` | Evacuation auto-assignment | Processes waiting citizens in urgency order |
| Greedy allocation | Resource supply planning (`greedyAllocate`) | Locally optimal grants with unmet demand reported; overflow impossible |
| HashMap indexes | `DataManager` ID lookups | O(1) validation of every cross-module reference |
| FIFO `LinkedList` queue | Response Center dispatch queue | Models the dispatcher's sequential workflow |
| LIFO stack (`ArrayList`) | Undo last delete | LIFO is exactly undo semantics |
| Merge sort (hand-written) | Sort resources by quantity | Stable O(n log n) sort demonstrated on table data |
| Binary search (hand-written) | Sorted-ID lookup helper | O(log n) search on ordered IDs |
| Insertion sort by cost (hand-written) | Emergency Map / Route Planning nearest-shelter ranking | Shelters are few; a stable O(n^2) insertion sort by route cost is simple and exact |
| 2D plotting of graph nodes | Emergency Map `MapCanvas` | Locations store normalized (x, y) coordinates; nodes are projected to the canvas while edges, weights and routing stay in the same `Graph` used by Dijkstra |

## Persistence

Data is saved to `data/sdrs_data.ser` after every change and on close; a
`.bak` copy is kept from the previous save. On startup the app loads the file,
falls back to the backup if the main file is corrupted, and falls back to
realistic seed data if neither exists. Deleting the `data` folder resets the
system to demo data.

## Resetting Demo Data

Stop the application, delete the `data` folder, start again.

## Notes for the Report

- Single-user file storage; the `DataStore` interface is the swap point for a
  database later.
- The road network is a logical graph (no GIS coordinates), so routes are
  schematic but algorithmically genuine.
- Passwords are stored as SHA-256 hashes, not plain text.
