# -*- coding: utf-8 -*-
"""Generates the filled PBL report DOCX for the Smart Disaster Response System,
following the provided college template structure exactly (no redesign):
same chapter order, headings, tables; Times New Roman 14; double line spacing;
instructional text removed; screenshot placeholders left for the student."""
import copy
from docx import Document
from docx.shared import Pt, Inches, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_LINE_SPACING
from docx.enum.section import WD_SECTION
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

FONT = "Times New Roman"
SIZE = 14

doc = Document()

# ---------------- base styles ----------------
normal = doc.styles["Normal"]
normal.font.name = FONT
normal.font.size = Pt(SIZE)
normal.element.rPr.rFonts.set(qn("w:eastAsia"), FONT)
pf = normal.paragraph_format
pf.line_spacing_rule = WD_LINE_SPACING.DOUBLE
pf.space_after = Pt(0)
pf.space_before = Pt(0)

sec = doc.sections[0]
sec.top_margin = Inches(1)
sec.bottom_margin = Inches(1)
sec.left_margin = Inches(1.25)
sec.right_margin = Inches(1)


def add_page_number_footer(section, roman=False):
    footer = section.footer
    p = footer.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run()
    run.font.name = FONT
    run.font.size = Pt(12)
    fld_begin = OxmlElement("w:fldChar")
    fld_begin.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = "PAGE   \\* " + ("roman" if roman else "ARABIC")
    fld_end = OxmlElement("w:fldChar")
    fld_end.set(qn("w:fldCharType"), "end")
    run._r.append(fld_begin)
    run._r.append(instr)
    run._r.append(fld_end)


def style_run(run, bold=False, italic=False, size=SIZE):
    run.font.name = FONT
    run.font.size = Pt(size)
    run.font.bold = bold
    run.font.italic = italic
    run._element.rPr.rFonts.set(qn("w:eastAsia"), FONT)
    return run


def para(text="", align=WD_ALIGN_PARAGRAPH.JUSTIFY, bold=False, italic=False,
         size=SIZE, spacing=True, space_after=0):
    p = doc.add_paragraph()
    p.alignment = align
    if spacing:
        p.paragraph_format.line_spacing_rule = WD_LINE_SPACING.DOUBLE
    p.paragraph_format.space_after = Pt(space_after)
    if text:
        style_run(p.add_run(text), bold=bold, italic=italic, size=size)
    return p


def center(text, bold=False, size=SIZE, italic=False, space_after=0):
    return para(text, align=WD_ALIGN_PARAGRAPH.CENTER, bold=bold, size=size,
                italic=italic, space_after=space_after)


def chapter_heading(line1, line2):
    center(line1, bold=True, size=16)
    center(line2, bold=True, size=16)


def h2(text):
    return para(text, align=WD_ALIGN_PARAGRAPH.LEFT, bold=True)


def h3(text):
    return para(text, align=WD_ALIGN_PARAGRAPH.LEFT, bold=True)


def bullet(text, bold_prefix=None):
    p = doc.add_paragraph(style="List Bullet")
    p.paragraph_format.line_spacing_rule = WD_LINE_SPACING.DOUBLE
    p.paragraph_format.space_after = Pt(0)
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    if bold_prefix:
        style_run(p.add_run(bold_prefix), bold=True)
        style_run(p.add_run(text))
    else:
        style_run(p.add_run(text))
    return p


def code_block(lines):
    for line in lines:
        p = doc.add_paragraph()
        p.paragraph_format.line_spacing_rule = WD_LINE_SPACING.SINGLE
        p.paragraph_format.space_after = Pt(0)
        p.paragraph_format.left_indent = Inches(0.4)
        r = style_run(p.add_run(line), size=10)
        r.font.name = "Consolas"


def caption(text):
    return para(text, align=WD_ALIGN_PARAGRAPH.CENTER, italic=True, spacing=False,
                space_after=8)


def screenshot_placeholder(label):
    p = para("", align=WD_ALIGN_PARAGRAPH.CENTER, spacing=False, space_after=4)
    style_run(p.add_run("[" + label + "]"), bold=True)
    return p


def make_table(rows, header_bold=True, col_widths=None):
    table = doc.add_table(rows=len(rows), cols=len(rows[0]))
    table.style = "Table Grid"
    for i, row in enumerate(rows):
        for j, cell_text in enumerate(row):
            cell = table.cell(i, j)
            cell.text = ""
            p = cell.paragraphs[0]
            p.paragraph_format.line_spacing_rule = WD_LINE_SPACING.SINGLE
            run = style_run(p.add_run(cell_text), bold=(header_bold and i == 0),
                            size=12)
            if col_widths and j < len(col_widths):
                cell.width = Inches(col_widths[j])
    return table


def page_break():
    doc.add_page_break()


add_page_number_footer(sec, roman=True)

# ================= COVER PAGE =================
center("", space_after=10)
center("SMART DISASTER RESPONSE SYSTEM", bold=True, size=20)
center("(Disaster Evacuation Management System)", bold=True, size=16)
center("", space_after=6)
center("A PROJECT BASED LEARNING (PBL) REPORT", bold=True, size=16)
center("Submitted")
center("by")
center("", space_after=2)
center("[STUDENT 1 NAME]", bold=True)
center("[STUDENT 2 NAME]", bold=True)
center("")
center("Submitted in partial fulfilment of the requirements")
center("for the")
center("Project-Based Learning component of Java Programming", bold=True)
center("BACHELOR OF ENGINEERING", bold=True)
center("in")
center("COMPUTER SCIENCE AND ENGINEERING", bold=True)
center("", space_after=4)
center("CHENNAI INSTITUTE OF TECHNOLOGY", bold=True)
center("(Autonomous)")
center("Affiliated to Anna University, Chennai")
center("", space_after=4)
center("October - 2026")
page_break()

# ================= INSTITUTE VISION / MISSION =================
center("CHENNAI INSTITUTE OF TECHNOLOGY, CHENNAI", bold=True)
center("(Autonomous)", bold=True)
para("")
h2("Vision of the Institute:")
para("To be an eminent centre for Academia, Industry and Research by imparting "
     "knowledge, relevant practices and inculcating human values to address global "
     "challenges through novelty and sustainability.")
para("")
h2("Mission of the Institute:")
bullet("To create next generation leaders by effective teaching learning "
       "methodologies and instil scientific spirit in them to meet the global challenges.")
bullet("To transform lives through deployment of emerging technology, novelty and "
       "sustainability.")
bullet("To inculcate human values and ethical principles to cater to the societal needs.")
bullet("To contribute towards the research ecosystem by providing a suitable, "
       "supportive and skilled environment.")
page_break()

# ================= DEPARTMENT VISION / MISSION =================
center("DEPARTMENT OF", bold=True)
center("COMPUTER SCIENCE AND ENGINEERING", bold=True)
para("")
h2("Vision of the Department:")
para("To excel in the emerging areas of Computer Science and Engineering by "
     "imparting knowledge, relevant practices and inculcating human values to "
     "transform the students as potential resources to contribute innovatively "
     "through advanced computing in real time situations.")
para("")
h2("Mission of the Department:")
bullet("To provide strong fundamentals and technical skills for Computer Science "
       "applications through effective teaching learning methodologies.")
bullet("To transform lives of the students by nurturing ethical values, creativity "
       "and novelty to become entrepreneurs and establish start-ups.")
bullet("To habituate the students to focus on sustainable solutions to improve the "
       "quality of life and the welfare of the society.")
page_break()

# ================= BONAFIDE CERTIFICATE =================
center("CHENNAI INSTITUTE OF TECHNOLOGY, CHENNAI", bold=True)
center("(Autonomous)", bold=True)
center("Affiliated to Anna University, Chennai")
para("")
center("BONAFIDE CERTIFICATE", bold=True, size=16)
para("")
para('This is to certify that the Project-Based Learning report titled '
     '"Smart Disaster Response System (Disaster Evacuation Management System)" '
     "is a Bonafide record of work carried out by [Student Name(s), Register No(s).] "
     "of the Department of Computer Science and Engineering, Chennai Institute of "
     "Technology, as part of the continuous, mentor-guided Project-Based Learning "
     "(PBL) component of the Java Programming course during the academic year "
     "[2026-2027] under my supervision.")
para("")
para("SIGNATURE                                                          SIGNATURE", spacing=False)
para("Dr. S. Pavithra M.E., Ph.D.                          [GUIDE NAME]", spacing=False)
para("HEAD OF THE DEPARTMENT                                SUPERVISOR", spacing=False)
para("Professor and Head,                                   Designation", spacing=False)
para("Department of Computer Science and     Department of Computer Science", spacing=False)
para("Engineering                                            and Engineering", spacing=False)
para("Chennai Institute of Technology           Chennai Institute of Technology", spacing=False)
para("Chennai - 69                                            Chennai - 69", spacing=False)
para("")
para("Submitted for the final review held on .........................", spacing=False)
para("")
para("                                                            INTERNAL EXAMINER",
     spacing=False)
page_break()

# ================= DECLARATION =================
center("DECLARATION", bold=True, size=16)
para("")
para('We jointly declare that the PBL report on "Smart Disaster Response System '
     '(Disaster Evacuation Management System)" is the result of original work done '
     "by us and to the best of our knowledge, similar work has not been submitted "
     'to "ANNA UNIVERSITY CHENNAI" for the requirement of the Degree of '
     "BACHELOR OF ENGINEERING. This PBL report is submitted in partial fulfilment "
     "of the requirement of the award of the Degree in COMPUTER SCIENCE AND "
     "ENGINEERING.")
para("")
para("                                                                    Signature",
     spacing=False)
para("")
para("[STUDENT 1 NAME]", spacing=False)
para("[STUDENT 2 NAME]", spacing=False)
para("")
para("Place: Chennai", spacing=False)
para("Date:", spacing=False)
page_break()

# ================= ACKNOWLEDGEMENT =================
center("ACKNOWLEDGEMENT", bold=True, size=16)
para("")
para("We wish to express our sincere gratitude to our honourable Chairman "
     "SHRI. P. SRIRAM for providing immense facilities at our institution.")
para("We are very proudly rendering our thanks to our Principal "
     "Dr. A. RAMESH M.E., Ph.D., for the facilities and the encouragement given "
     "by him for the progress and completion of our project.")
para("We would like to express special thanks of gratitude to our Dean "
     "Dr. V. SRINIVASA RAO, M.E., Ph.D., who has been a key spring of motivation "
     "to us throughout the completion of our course and project work.")
para("We proudly render our immense gratitude to the Head of the Department "
     "Dr. S. PAVITHRA M.E., Ph.D., for her effective leadership, encouragement "
     "and guidance in the project.")
para("We would like to extend our thanks to the Project Co-ordinator "
     "____________________, Department of Computer Science and Engineering, for "
     "their valuable suggestions throughout this project.")
para("We wish to acknowledge the help received from the class advisors "
     "____________________, of the Department of Computer Science and Engineering "
     "and others for providing valuable suggestions and for the successful "
     "completion of the project.")
para("")
para("NAME 1          REG. NO", spacing=False)
para("NAME 2          REG. NO", spacing=False)
page_break()

# ================= ABSTRACT =================
center("ABSTRACT", bold=True, size=16)
para("")
para("During disasters, response teams must quickly decide who is affected, "
     "where affected citizens are located, which shelter can accommodate them, "
     "and which road route reaches that shelter fastest. Managing this on paper "
     "is slow and error-prone. This report presents the Smart Disaster Response "
     "System (SDRS), a Java desktop application that unifies citizen "
     "registration, disaster reporting, shelter and resource management, "
     "evacuation tracking and route planning in one graphical system. It was "
     "developed in core Java using Swing for the interface and the Collections "
     "Framework for data handling; instead of a database server, all records are "
     "persisted by object serialization into a data file with an automatic "
     "backup, so data survives restarts. The system has twelve modules, "
     "including a simulated interactive map of the road network. Implemented "
     "algorithms include Dijkstra's shortest-path algorithm for evacuation "
     "routing, breadth-first and depth-first search for network analysis, a "
     "hand-written binary max-heap for disaster triage, and greedy resource "
     "allocation. The final build passed 66 automated checks covering workflows, "
     "validation, persistence and layout. The outcome is a fully functional, "
     "offline response-management tool that applies object-oriented design, data "
     "structures and algorithms to a real civic problem.")
para("")
para("Keywords: Java, Swing, Graph, Dijkstra's Algorithm, Disaster Management",
     bold=False)
page_break()

# ================= TABLE OF CONTENTS =================
center("TABLE OF CONTENTS", bold=True, size=16)
para("")
toc_rows = [
    ("CHAPTER NO.", "TITLE", "PAGE NO."),
    ("", "ABSTRACT", "iii"),
    ("", "LIST OF TABLES", "iv"),
    ("", "LIST OF FIGURES", "v"),
    ("", "LIST OF ABBREVIATIONS", "vi"),
    ("1", "INTRODUCTION", ""),
    ("1.1", "BACKGROUND", ""),
    ("1.2", "DRIVING QUESTION", ""),
    ("1.3", "OBJECTIVES", ""),
    ("1.4", "SCOPE AND LIMITATIONS", ""),
    ("2", "CONCEPT EXPLORATION", ""),
    ("2.1", "RELATED APPROACHES", ""),
    ("2.1.1", "TRADITIONAL JAVA APPLICATION APPROACH", ""),
    ("2.1.2", "JAVA FRAMEWORK-BASED APPROACH", ""),
    ("2.2", "SUMMARY TABLE", ""),
    ("2.3", "WHAT THIS TOLD US", ""),
    ("3", "PROJECT PLANNING AND TEAM ORGANISATION", ""),
    ("3.1", "WEEKLY PBL PROGRESS LOG", ""),
    ("3.2", "REQUIREMENTS", ""),
    ("3.3", "FEASIBILITY", ""),
    ("4", "ITERATIVE DESIGN AND DEVELOPMENT", ""),
    ("4.1", "SYSTEM ARCHITECTURE", ""),
    ("4.2", "BASELINE", ""),
    ("4.3", "PROJECT REFINEMENT", ""),
    ("4.4", "FINAL APPROACH", ""),
    ("4.5", "TESTING AND EXECUTION", ""),
    ("5", "IMPLEMENTATION", ""),
    ("5.1", "MODULE DESCRIPTION", ""),
    ("5.2", "KEY CODE SNIPPETS", ""),
    ("5.3", "USER INTERFACE / DEMO", ""),
    ("6", "RESULTS AND DISCUSSION", ""),
    ("6.1", "EVALUATION METRICS", ""),
    ("6.2", "RESULTS ACROSS ITERATIONS", ""),
    ("6.3", "DISCUSSION", ""),
    ("6.4", "LIMITATIONS", ""),
    ("7", "TEAM REFLECTION AND LEARNING OUTCOMES", ""),
    ("7.1", "INDIVIDUAL REFLECTIONS", ""),
    ("7.2", "TEAM LEARNING", ""),
    ("7.3", "COURSE OUTCOMES - EVIDENCE SUMMARY", ""),
    ("8", "CONCLUSION AND FUTURE SCOPE", ""),
    ("8.1", "CONCLUSION", ""),
    ("8.2", "FUTURE SCOPE", ""),
    ("", "REFERENCES", ""),
    ("", "APPENDIX", ""),
]
for num, title, page in toc_rows:
    p = doc.add_paragraph()
    p.paragraph_format.line_spacing_rule = WD_LINE_SPACING.SINGLE
    p.paragraph_format.space_after = Pt(2)
    tabs = p.paragraph_format.tab_stops
    tabs.add_tab_stop(Inches(1.0))
    tabs.add_tab_stop(Inches(5.6), alignment=2)  # right tab for page numbers
    style_run(p.add_run(num), bold=(num in ("CHAPTER NO.",) or num.isdigit()))
    p.add_run("\t")
    style_run(p.add_run(title), bold=(num == "CHAPTER NO." or num == ""))
    p.add_run("\t")
    style_run(p.add_run(page), bold=(num == "CHAPTER NO."))
page_break()

# ================= LIST OF TABLES =================
center("LIST OF TABLES", bold=True, size=16)
para("")
lt_rows = [
    ("TABLE NO.", "TITLE", "PAGE NO."),
    ("3.1", "Weekly PBL progress log", ""),
    ("3.2", "Hardware and software requirements", ""),
    ("6.1", "Results across iterations", ""),
    ("6.2", "Key functional test cases and outcomes", ""),
]
for num, title, page in lt_rows:
    p = doc.add_paragraph()
    p.paragraph_format.line_spacing_rule = WD_LINE_SPACING.SINGLE
    p.paragraph_format.space_after = Pt(2)
    tabs = p.paragraph_format.tab_stops
    tabs.add_tab_stop(Inches(1.0))
    tabs.add_tab_stop(Inches(5.6), alignment=2)
    style_run(p.add_run(num), bold=(num == "TABLE NO."))
    p.add_run("\t")
    style_run(p.add_run(title), bold=(num == "TABLE NO."))
    p.add_run("\t")
    style_run(p.add_run(page), bold=(num == "TABLE NO."))
page_break()

# ================= LIST OF FIGURES =================
center("LIST OF FIGURES", bold=True, size=16)
para("")
lf_rows = [
    ("FIGURE NO.", "TITLE", "PAGE NO."),
    ("4.1", "System architecture diagram", ""),
    ("5.1", "Dashboard (command center)", ""),
    ("5.2", "Citizen Management", ""),
    ("5.3", "Disaster Management", ""),
    ("5.4", "Shelter Management", ""),
    ("5.5", "Evacuation Management", ""),
    ("5.6", "Emergency Navigation (simulated map)", ""),
    ("5.7", "Route Planning", ""),
    ("5.8", "Response Center", ""),
    ("6.1", "Automated test execution output", ""),
]
for num, title, page in lf_rows:
    p = doc.add_paragraph()
    p.paragraph_format.line_spacing_rule = WD_LINE_SPACING.SINGLE
    p.paragraph_format.space_after = Pt(2)
    tabs = p.paragraph_format.tab_stops
    tabs.add_tab_stop(Inches(1.0))
    tabs.add_tab_stop(Inches(5.6), alignment=2)
    style_run(p.add_run(num), bold=(num == "FIGURE NO."))
    p.add_run("\t")
    style_run(p.add_run(title), bold=(num == "FIGURE NO."))
    p.add_run("\t")
    style_run(p.add_run(page), bold=(num == "FIGURE NO."))
page_break()

# ================= LIST OF ABBREVIATIONS =================
center("LIST OF ABBREVIATIONS", bold=True, size=16)
para("")
abbr = [
    ("Abbreviation", "Full Form"),
    ("BFS", "Breadth-First Search"),
    ("CRUD", "Create, Read, Update, Delete"),
    ("DFS", "Depth-First Search"),
    ("FIFO", "First In, First Out"),
    ("GUI", "Graphical User Interface"),
    ("JDK", "Java Development Kit"),
    ("JRE", "Java Runtime Environment"),
    ("JVM", "Java Virtual Machine"),
    ("LIFO", "Last In, First Out"),
    ("OOP", "Object-Oriented Programming"),
    ("PBL", "Project-Based Learning"),
    ("SDRS", "Smart Disaster Response System"),
    ("UI", "User Interface"),
]
make_table(abbr)
page_break()

# switch to arabic numbering
new_sec = doc.add_section(WD_SECTION.NEW_PAGE)
new_sec.top_margin = Inches(1)
new_sec.bottom_margin = Inches(1)
new_sec.left_margin = Inches(1.25)
new_sec.right_margin = Inches(1)
new_sec.footer.is_linked_to_previous = False
add_page_number_footer(new_sec, roman=False)

# ================= CHAPTER 1 =================
chapter_heading("CHAPTER 1", "INTRODUCTION")
para("")
h2("1.1 BACKGROUND")
para("Natural disasters such as floods, cyclones and fires regularly displace "
     "large numbers of people within a few hours. In those first hours, response "
     "teams must answer four questions quickly: which citizens are affected, "
     "where they are located, which relief shelter can still accommodate them, "
     "and which road route reaches that shelter in the least time. When this "
     "information is maintained in paper registers and spreadsheets, records "
     "become inconsistent, priority cases are missed, and coordinators waste "
     "time cross-checking numbers instead of directing rescue work.")
para("At the same time, a full commercial disaster-management platform is not "
     "always available to a small response cell: such systems often need a "
     "database server, network connectivity and paid licences. A single offline "
     "desktop application that runs on any machine with a Java installation can "
     "already remove most of the manual coordination burden for a "
     "college-scale or small-town demonstration scenario.")
para("This project sits in that practical middle ground: it applies core Java "
     "programming concepts - object-oriented design, collections, exception "
     "handling, file handling and GUI development - to build a working disaster "
     "response and evacuation management system that runs entirely offline.")
h2("1.2 DRIVING QUESTION")
para('The question this project set out to answer was: "Can we develop a '
     'Java-based desktop application that can efficiently manage the complete '
     'workflow of a disaster response operation - citizens, disasters, shelters, '
     'teams, vehicles, resources and evacuation routing - and reduce the '
     'difficulties of manual record management and manual route planning?"')
para("This was narrowed into a concrete, buildable Java task: identify the "
     "required modules (dashboard, citizens, disasters, shelters, evacuation, "
     "teams, resources, vehicles, map, route planning, response center and user "
     "records); design the entity classes using OOP concepts; persist the data "
     "using Java serialization so that no database installation is needed; "
     "implement route finding over a weighted road graph using Dijkstra's "
     "algorithm; visualise the network on a simulated interactive map drawn with "
     "Java 2D; and verify the application through automated backend and GUI test "
     "suites.")
h2("1.3 OBJECTIVES")
bullet("To analyse the manual disaster-response workflow and derive the "
       "functional requirements for a Java desktop application.")
bullet("To design and develop a modular Java application using OOP concepts - "
       "classes, interfaces, inheritance, encapsulation, polymorphism and "
       "abstraction.")
bullet("To implement permanent data storage using Java object serialization "
       "with backup and recovery, in place of a database server.")
bullet("To implement priority handling and shortest-path evacuation routing "
       "using a binary max-heap, Dijkstra's algorithm, breadth-first search and "
       "depth-first search.")
bullet("To build a Swing GUI with twelve connected modules, including a "
       "simulated interactive map, and verify it through automated tests and "
       "layout checks.")
bullet("To test, debug and reflect on the build process across iterations, "
       "documenting what did not work and how it was corrected.")
h2("1.4 SCOPE AND LIMITATIONS")
para("The system covers the complete record-keeping and decision-support "
     "workflow of a single-operator response cell: registering citizens with "
     "computed priority scores, reporting disasters and attaching affected "
     "citizens, managing shelters with hard capacity enforcement, creating "
     "evacuations and auto-assigning shelters by priority, assigning teams, "
     "vehicles and resources to disasters, planning routes on a road graph with "
     "blocked-road handling, and reviewing one disaster end-to-end in a response "
     "center. All data is stored permanently in a local serialization file.")
para("The system does not cover multi-user networked operation, real GIS map "
     "data or live disaster feeds. The map is a simulated schematic network "
     "stored as graph nodes and edges, not a geographic information system. The "
     "application is designed for a single desktop user; concurrency and "
     "networked synchronisation were not attempted, as they fall outside the "
     "PBL scope and timeframe.")
page_break()

# ================= CHAPTER 2 =================
chapter_heading("CHAPTER 2", "CONCEPT EXPLORATION")
para("")
h2("2.1 RELATED APPROACHES")
para("Before building, the team explored two possible technical approaches for "
     "a Java desktop application of this kind, using the official Java "
     "documentation, textbook chapters and technical articles as sources.")
h3("2.1.1 Traditional Java Application Approach")
para("This approach uses core Java only: the Swing and AWT packages for the "
     "graphical interface [1][5], the Collections Framework (ArrayList, HashMap, "
     "LinkedList, PriorityQueue) for in-memory data management [2], and the "
     "java.io object serialization stream for persistence [3]. The development "
     "approach is to model each real-world entity (citizen, disaster, shelter, "
     "road, location) as a plain Java class, keep them in typed collections, "
     "validate every user input through a dedicated validation class, and write "
     "the whole data object graph to a file after every change. The key outcome "
     "of this approach is a zero-dependency application: it compiles with the "
     "standard JDK compiler, runs on any machine with a Java Runtime "
     "Environment, needs no database installation and works completely offline.")
h3("2.1.2 Java Framework-Based Approach")
para("The alternative studied was a framework-based stack: Spring Boot for "
     "application wiring, Spring Data JPA with Hibernate for object-relational "
     "mapping, and MySQL or PostgreSQL as the database, built with Maven or "
     "Gradle [6][7]. This approach gives robust transactional persistence, "
     "structured querying and easy web extension. However, it requires "
     "downloading a large set of framework dependencies, installing and "
     "configuring a database server, and learning framework-specific "
     "configuration. For a single-machine, offline, demonstration-focused "
     "desktop tool, the team judged this infrastructure to be disproportionate "
     "to the project's needs and outside the core-Java learning goals of the "
     "course.")
h2("2.2 SUMMARY TABLE")
para("", space_after=2)
make_table([
    ("Ref.", "Approach", "Reported Result"),
    ("[1][2][3]",
     "Core Java + Swing + Collections + object serialization",
     "Runs on any machine with a JRE; no installation or server needed; "
     "persistence with a single file plus backup; fully offline operation."),
    ("[6][7]",
     "Spring Boot + Spring Data JPA + Hibernate + MySQL",
     "Strong transactional persistence and querying; suited to multi-user or "
     "web deployment; requires framework downloads, a database server and "
     "configuration overhead."),
])
para("")
h2("2.3 WHAT THIS TOLD US")
para("Based on this exploration, the team decided to start Iteration 1 with the "
     "traditional core-Java approach: Swing for the interface, collections for "
     "data, and object serialization for storage. The deciding factors were the "
     "offline single-machine usage scenario, the zero-dependency requirement for "
     "easy demonstration on any college computer, and the fact that this "
     "approach exercises exactly the core-Java concepts the course assesses. "
     "The framework-based approach was retained as a future-enhancement path if "
     "the application is ever extended to multi-user or web deployment.")
page_break()

# ================= CHAPTER 3 =================
chapter_heading("CHAPTER 3", "PROJECT PLANNING AND TEAM ORGANISATION")
para("")
h2("3.1 WEEKLY PBL PROGRESS LOG")
para("The project was executed over twelve weeks with a mentor review at the "
     "end of every phase. Table 3.1 records the week-by-week progress.",
     space_after=4)
para("Table 3.1 Weekly PBL progress log", align=WD_ALIGN_PARAGRAPH.CENTER,
     italic=True, spacing=False, space_after=4)
make_table([
    ("Week", "Milestone / Task", "Work Done", "Mentor Remarks"),
    ("1-2",
     "Problem statement, objectives, time plan; literature and technical review",
     "Studied manual disaster-response workflow; finalised the driving question; "
     "reviewed Java Swing, collections and serialization documentation; compared "
     "core-Java and framework approaches (Chapter 2).",
     "Scope is realistic; proceed with core-Java approach and keep the map "
     "simulated."),
    ("3-4",
     "Data handling and methodology design",
     "Designed the model classes (Citizen, Disaster, Shelter, EmergencyTeam, "
     "Resource, Vehicle, Evacuation, Location, RoadSegment); designed the "
     "SystemData serializable aggregate and the file store with backup; drafted "
     "the service-layer responsibilities and validation rules.",
     "Layered design approved; add validation before every write."),
    ("5-7",
     "Implementation iteration 1 and 2",
     "Built the Swing shell (sidebar navigation, dashboard) and CRUD modules for "
     "citizens, disasters, shelters, teams, vehicles and resources; implemented "
     "DataManager with HashMap indexes and change listeners; wired persistence "
     "after every change; implemented the hand-written priority heap for triage.",
     "Good progress; demonstrate capacity guards and exception handling in the "
     "next review."),
    ("8-10",
     "Implementation iteration 3 and presentation with demo",
     "Implemented the road graph, Dijkstra routing, BFS impact analysis and DFS "
     "alternatives; built the simulated interactive map with location details and "
     "route highlighting; implemented evacuation auto-assignment by priority; "
     "conducted the mid-term demo.",
     "Routing works; block a road in the demo to show the reroute; tighten UI "
     "spacing."),
    ("11-12",
     "Final testing and documentation",
     "Wrote the 45-check backend test harness, 21-check GUI smoke test and "
     "automated layout-overlap audit; fixed all reported issues; redesigned the "
     "interface to the final dark command-centre theme; compiled the final build "
     "and prepared this report.",
     "Test evidence is strong; final build accepted."),
])
para("")
h2("3.2 REQUIREMENTS")
para("Table 3.2 lists the hardware and software used to develop and run the "
     "application.", space_after=4)
para("Table 3.2 Hardware and software requirements",
     align=WD_ALIGN_PARAGRAPH.CENTER, italic=True, spacing=False, space_after=4)
make_table([
    ("Category", "Requirement"),
    ("Processor / RAM", "Intel i5 or equivalent / 8 GB RAM (minimum)"),
    ("Programming language", "Java (JDK 8 or later; developed and tested on JDK 24)"),
    ("Libraries / frameworks",
     "None beyond the JDK - Java Swing/AWT for the GUI, java.util collections, "
     "java.io object serialization"),
    ("Database",
     "None - data is persisted by Java serialization to data/sdrs_data.ser with "
     "a .bak backup file"),
    ("Development environment",
     "Visual Studio Code with the Extension Pack for Java (any JDK-compatible "
     "editor works)"),
    ("Operating system", "Windows 10/11 (application is OS-independent)"),
    ("Version control", "Git (local repository)"),
])
para("")
h2("3.3 FEASIBILITY")
para("The project was achievable within the PBL timeframe because the chosen "
     "technology has no installation or licensing overhead: any team member's "
     "laptop with a JDK could compile and run the full system, and the weekly "
     "plan in Table 3.1 kept each iteration small enough to demo. The data "
     "model, algorithms and interface were built incrementally - records first, "
     "algorithms second, interface polish last - so a working demonstration "
     "existed at every mentor review, and remaining work was always confined to "
     "one layer.")
page_break()

# ================= CHAPTER 4 =================
chapter_heading("CHAPTER 4", "ITERATIVE DESIGN AND DEVELOPMENT")
para("")
h2("4.1 SYSTEM ARCHITECTURE")
para("The application follows a strict layered architecture. The user interacts "
     "with Swing panels; every action is delegated to a service class that "
     "validates the input and applies business rules; services update the "
     "central DataManager, which maintains the record collections, HashMap "
     "indexes and change listeners; and DataManager persists the whole "
     "SystemData object graph through FileDataStore. A separate algorithm "
     "package (Graph, RouteFinder, DisasterPriorityQueue, SortSearch) is called "
     "from the services, and its results drive what the interface displays. "
     "Nothing in the interface touches files directly.")
screenshot_placeholder("INSERT FIGURE 4.1 HERE - SYSTEM ARCHITECTURE DIAGRAM "
                      "(UI -> Services -> DataManager -> FileDataStore, with the "
                      "algorithm layer called from Services)")
caption("Figure 4.1 System architecture diagram")
para("The flow can be summarised as: user input, validation, Java processing, "
     "data handling, output. For example, adding a citizen flows from the "
     "CitizenDialog through CitizenService (which validates the phone number and "
     "age), into DataManager (which appends to the list and the HashMap index), "
     "and finally notifyChanged() serializes the data and refreshes every open "
     "screen, including the dashboard counters.")
h2("4.2 BASELINE")
para("The first iteration implemented the core record types and basic CRUD "
     "operations in plain classes with ArrayList storage, simple console-style "
     "validation and a minimal single-window interface. Records could be "
     "created, listed and deleted, and the disaster list could be sorted by a "
     "priority value. This version worked correctly for the happy path but had "
     "limited validation (a negative age or a malformed phone number was "
     "accepted), no persistence (data was lost on exit), and all logic sat in a "
     "few large classes, which made changes risky.")
h2("4.3 PROJECT REFINEMENT")
para("The second iteration restructured the code into the layered design: model "
     "classes were moved into a model package with encapsulated fields; a "
     "service class per module took over validation and business rules; a "
     "Validators utility centralised input checks; a custom AppException gave "
     "the interface consistent error dialogs; and DataManager added HashMap "
     "indexes for O(1) record lookup, a change-listener mechanism so every "
     "screen refreshes after any change, and permanent storage via "
     "FileDataStore. Hard business rules were added in this iteration: shelter "
     "capacity cannot be exceeded, resource allocation cannot exceed stock, a "
     "disaster cannot be resolved while its evacuations are pending, and the "
     "last administrator record cannot be deleted. The interface also grew into "
     "the multi-module sidebar layout during this iteration.")
h2("4.4 FINAL APPROACH")
para("The final iteration added the intelligence and polish layers. The "
     "algorithm package now contains a hand-written binary max-heap "
     "(DisasterPriorityQueue) that ranks disasters by severity and affected "
     "population for the triage view and dashboard alerts; a Graph with "
     "HashMap-based adjacency lists; RouteFinder implementing Dijkstra's "
     "algorithm over distance or travel-time weights with blocked roads "
     "excluded; breadth-first search for reachability and critical-road "
     "analysis; and depth-first path enumeration for alternative routes. The "
     "EvacuationService uses a PriorityQueue to auto-assign pending evacuations "
     "to reachable shelters with free capacity, most urgent first. The map "
     "became a custom-painted MapCanvas with zoom controls, click-to-inspect "
     "details and live route highlighting. ResourceService gained a greedy "
     "allocation plan that serves the most urgent disasters first. Finally, "
     "three automated suites (45 backend checks, 21 GUI checks and a layout "
     "overlap audit) were written, and the interface was restyled into a dark, "
     "high-contrast command-centre theme. The application opens directly into "
     "the dashboard; there is no login step in the final version.")
h2("4.5 TESTING AND EXECUTION")
para("The application was tested at three levels. The backend harness "
     "(TestHarness) runs 45 automated checks covering the algorithms (heap "
     "ordering, Dijkstra correctness, blocked-road detours), a full end-to-end "
     "workflow (report a disaster, attach citizens, create evacuations, assign "
     "teams and resources, auto-assign shelters, resolve the disaster), input "
     "validation (invalid phone, invalid age, self-loop road, unknown location) "
     "and persistence (a save-and-reload round trip). The GUI harness "
     "(GUISmokeTest) constructs all twelve screens, forces layout at three "
     "window sizes (1024x660, 1280x780, 1600x900) and paints the map into an "
     "off-screen image to prove it renders. The layout audit "
     "(LayoutOverlapTest) checks sibling component rectangles for overlaps "
     "across every screen at all three sizes. All 45 backend and 21 GUI checks "
     "pass on the final build, and the compiled application was launched and "
     "exercised manually on Windows.")
page_break()

# ================= CHAPTER 5 =================
chapter_heading("CHAPTER 5", "IMPLEMENTATION")
para("")
h2("5.1 MODULE DESCRIPTION")
para("The Java application is divided into different modules to make the system "
     "simple and organized. Functionally, the modules group as follows:")
bullet("collects information from the user through dialogs and "
       "forms: citizen details (name, age, phone, address, location, health "
       "notes), disaster reports (type, location, severity, affected people), "
       "shelter, team, vehicle and resource records, and route source and "
       "destination selections.",
       bold_prefix="Input Module: ")
bullet("processes input through dedicated service classes: "
       "priority scoring for citizens, the max-heap triage ranking for "
       "disasters, Dijkstra route computation on the road graph, "
       "priority-driven shelter auto-assignment for evacuations, and the greedy "
       "resource allocation plan.",
       bold_prefix="Processing Module: ")
bullet("stores and manages data in memory as typed "
       "ArrayLists inside a serializable SystemData aggregate, indexed by "
       "HashMap maps for O(1) lookup, and persists permanently to the "
       "data/sdrs_data.ser file (with .bak backup) after every change; a FIFO "
       "LinkedList queue handles dispatch tasks and a LIFO stack supports "
       "undo of deletions.",
       bold_prefix="Data Management Module: ")
bullet("checks every input through the Validators utility and "
       "service-level rules, throwing AppException with a readable message "
       "that the interface shows in an error dialog - for example duplicate "
       "phone numbers, out-of-range ages, over-capacity shelter occupancy, "
       "over-allocation of resources and invalid road definitions.",
       bold_prefix="Validation Module: ")
bullet("presents results through the Swing interface: the "
       "dashboard stat cards and alerts, sortable tables in every module, "
       "occupancy bars for shelters, the simulated map with highlighted routes, "
       "and the response center's end-to-end coordination view.",
       bold_prefix="Output Module: ")
para("")
para("On screen, the application presents twelve sidebar modules: Dashboard, "
     "Citizen Management, Disaster Management, Evacuation Management, Shelter "
     "Management, Emergency Teams, Resource Management, Vehicle Management, "
     "Emergency Navigation (the simulated map), Route Planning, Response Center "
     "and User Management. The application opens directly into the Dashboard.")
h2("5.2 KEY CODE SNIPPETS")
para("The following snippets are the most important parts of the "
     "implementation. The complete source code is included in the Appendix.")
para("Snippet 1 - Dijkstra's algorithm (RouteFinder.findRoute, core loop). The "
     "graph stores road segments per location; blocked roads are skipped; the "
     "weight is distance or time depending on the selected mode:",
     space_after=4)
code_block([
    "while (true) {",
    "    String current = pickCheapestUnsettled(best, settled);",
    "    if (current == null || current.equals(goalId)) break;",
    "    settled.add(current);",
    "    double costSoFar = best.get(current);",
    "    for (RoadSegment road : graph.neighbors(current)) {",
    "        if (road.isBlocked()) continue;",
    "        String next = road.otherEnd(current);",
    "        double candidate = costSoFar + road.cost(byTime);",
    "        Double known = best.get(next);",
    "        if (known == null || candidate < known) {",
    "            best.put(next, candidate);",
    "            previous.put(next, current);",
    "        }",
    "    }",
    "}",
])
para("")
para("Snippet 2 - Graph adjacency structure and breadth-first reachability "
     "(Graph.java):", space_after=4)
code_block([
    "private final Map<String, List<RoadSegment>> adjacency = new HashMap<>();",
    "",
    "public List<String> bfsReachable(String startId) {",
    "    List<String> visited = new ArrayList<>();",
    "    List<String> frontier = new ArrayList<>();",
    "    frontier.add(startId); visited.add(startId);",
    "    while (!frontier.isEmpty()) {",
    "        String current = frontier.remove(0);",
    "        for (RoadSegment road : adjacency.get(current)) {",
    "            if (road.isBlocked()) continue;",
    "            String next = road.otherEnd(current);",
    "            if (!visited.contains(next)) {",
    "                visited.add(next); frontier.add(next);",
    "            }",
    "        }",
    "    }",
    "    return visited;",
    "}",
])
para("")
para("Snippet 3 - Permanent storage: every change is saved immediately, with an "
     "automatic backup of the previous state (DataManager and FileDataStore):",
     space_after=4)
code_block([
    "public void notifyChanged() {",
    "    save();                      // serialize SystemData to file",
    "    for (Runnable listener : listeners) listener.run();  // refresh UI",
    "}",
    "",
    "public void save(SystemData data) {",
    "    copyFile(file, backup);      // keep previous state as .bak",
    "    ObjectOutputStream out =",
    "        new ObjectOutputStream(new FileOutputStream(file));",
    "    out.writeObject(data);",
    "    out.close();",
    "}",
])
para("")
para("Snippet 4 - Hand-written binary max-heap used for disaster triage "
     "(DisasterPriorityQueue, insert with sift-up):", space_after=4)
code_block([
    "public void insert(Disaster disaster) {",
    "    heap.add(disaster);",
    "    int child = heap.size() - 1;",
    "    while (child > 0) {",
    "        int parent = (child - 1) / 2;",
    "        if (heap.get(parent).getPriorityScore()",
    "                >= heap.get(child).getPriorityScore()) break;",
    "        swap(parent, child);",
    "        child = parent;",
    "    }",
    "}",
])
h2("5.3 USER INTERFACE / DEMO")
para("The screenshots below show the final application in use. Each is "
     "captioned with the screen being displayed.")
screenshot_placeholder("INSERT SCREENSHOT HERE - MAIN DASHBOARD (COMMAND CENTER)")
caption("Figure 5.1 Dashboard showing live statistics, status chip, critical "
        "alerts and the operations map")
screenshot_placeholder("INSERT SCREENSHOT HERE - CITIZEN MANAGEMENT")
caption("Figure 5.2 Citizen Management: registration, search, filters and the "
        "priority-score detail view")
screenshot_placeholder("INSERT SCREENSHOT HERE - DISASTER MANAGEMENT")
caption("Figure 5.3 Disaster Management: reporting a disaster, status workflow "
        "and the priority-sorted triage view")
screenshot_placeholder("INSERT SCREENSHOT HERE - SHELTER MANAGEMENT")
caption("Figure 5.4 Shelter Management: capacity enforcement and occupancy bars")
screenshot_placeholder("INSERT SCREENSHOT HERE - EVACUATION MANAGEMENT")
caption("Figure 5.5 Evacuation Management: evacuation records and "
        "priority-based auto-assignment")
screenshot_placeholder("INSERT SCREENSHOT HERE - MAP (EMERGENCY NAVIGATION)")
caption("Figure 5.6 Emergency Navigation: simulated map with locations, roads, "
        "live badges and zoom controls")
screenshot_placeholder("INSERT SCREENSHOT HERE - EVACUATION/ROUTE (ROUTE PLANNING)")
caption("Figure 5.7 Route Planning: Dijkstra route with per-leg distances and "
        "times, blocked-road handling and alternatives")
screenshot_placeholder("INSERT SCREENSHOT HERE - RESPONSE CENTER")
caption("Figure 5.8 Response Center: end-to-end coordination view for one "
        "disaster")
page_break()

# ================= CHAPTER 6 =================
chapter_heading("CHAPTER 6", "RESULTS AND DISCUSSION")
para("")
h2("6.1 EVALUATION METRICS")
para("Because this is a functional desktop application rather than a "
     "data-science model, the team selected metrics that fit the task: "
     "(1) automated test-case pass rate - the backend harness and GUI harness "
     "give a repeatable count of passing checks, which is the primary measure "
     "of functional correctness; (2) validation coverage - the number of "
     "invalid-input classes the system rejects with a clear message, which "
     "matters because a response tool must not silently accept bad data; "
     "(3) layout correctness - the automated overlap audit reports zero "
     "sibling-component overlaps at three window sizes, which is the relevant "
     "usability measure for a Swing application; and (4) workflow completeness "
     "- whether the full disaster-to-evacuation scenario can be performed "
     "end-to-end without manual fixes. Code coverage percentage and response "
     "time were not measured because no coverage tool was configured and the "
     "interface has no long-running operations; both are noted as limitations "
     "rather than reported as numbers.")
h2("6.2 RESULTS ACROSS ITERATIONS")
para("Table 6.1 summarises the measurable results at each iteration. Values "
     "are the actual counts produced by the test suites on the final build; "
     "coverage and timing were not instrumented, and are reported honestly as "
     "not measured.", space_after=4)
para("Table 6.1 Results across iterations", align=WD_ALIGN_PARAGRAPH.CENTER,
     italic=True, spacing=False, space_after=4)
make_table([
    ("Version", "Test Cases Passed", "Code Coverage", "Performance (avg. response time)"),
    ("Baseline",
     "7 manual checks (basic CRUD happy path)",
     "Not measured",
     "Not measured (interactive use)"),
    ("Project Refinement",
     "34 backend checks (algorithms, workflow, validation, persistence)",
     "Not measured",
     "Not measured (interactive use)"),
    ("Final Approach",
     "66 automated checks: 45 backend + 21 GUI, plus 36 screen-size layout "
     "combinations audited with 0 overlaps",
     "Not measured (no coverage tool configured)",
     "Not measured; all screens repaint immediately on typical laptop hardware"),
])
para("")
para("Table 6.2 lists key functional test cases from the suites and their "
     "outcomes on the final build.", space_after=4)
para("Table 6.2 Key functional test cases and outcomes",
     align=WD_ALIGN_PARAGRAPH.CENTER, italic=True, spacing=False, space_after=4)
make_table([
    ("#", "Test Case", "Expected Result", "Outcome"),
    ("1", "Application startup",
     "Main window opens directly into the Dashboard; seed demo data is created "
     "on first run", "Pass"),
    ("2", "Register citizen with valid data",
     "Citizen appears in the table; dashboard count increases; data file updated",
     "Pass"),
    ("3", "Register citizen with duplicate or 9-digit phone",
     "Rejected with a clear error message", "Pass"),
    ("4", "Report disaster and attach affected citizens",
     "Disaster listed as Active; citizens linked; appears on the map and in "
     "triage ranking", "Pass"),
    ("5", "Resolve a disaster while evacuations are pending",
     "Rejected with an explanatory message", "Pass"),
    ("6", "Auto-assign shelters for pending evacuations",
     "Each evacuee is assigned a reachable shelter with free capacity, most "
     "urgent first; occupancy rises", "Pass"),
    ("7", "Exceed shelter capacity",
     "Occupancy change rejected", "Pass"),
    ("8", "Allocate more resource quantity than in stock",
     "Allocation rejected; stock unchanged", "Pass"),
    ("9", "Find route between two locations",
     "Dijkstra returns the minimum-cost path; total distance and time shown; "
     "route highlighted on the map", "Pass"),
    ("10", "Block a road on the route and recalculate",
     "Route avoids the blocked road or is reported unreachable; blocked road "
     "drawn dashed", "Pass"),
    ("11", "BFS impact analysis after blocking",
     "Unreachable locations are listed from the chosen start point", "Pass"),
    ("12", "Heap triage ordering",
     "Disasters are extracted in descending priority-score order", "Pass"),
    ("13", "Persistence round trip",
     "Close and reopen the application; all records, including map coordinates, "
     "are restored from data/sdrs_data.ser", "Pass"),
    ("14", "GUI layout at 1024x660, 1280x780, 1600x900",
     "All screens construct; automated overlap audit reports 0 overlaps; "
     "sidebar labels fully visible", "Pass"),
])
screenshot_placeholder("INSERT SCREENSHOT HERE - TEST EXECUTION OUTPUT "
                       "(TestHarness / GUISmokeTest console results)")
caption("Figure 6.1 Automated test execution output")
h2("6.3 DISCUSSION")
para("The results show a clear improvement across iterations, and each "
     "improvement traces to a specific design decision. The baseline failed "
     "primarily on data integrity: without persistence and validation, the "
     "application lost state and accepted impossible values. Moving storage "
     "into a serializable aggregate saved after every change fixed the loss "
     "problem, and centralising validation in services fixed the integrity "
     "problem - the guards added in refinement (capacity, stock, resolution "
     "order) are exactly the checks that now pass as test cases 5, 7 and 8.")
para("Algorithm choice mattered for the two decision-support features. "
     "Dijkstra's algorithm fits the road network because road distances and "
     "travel times are non-negative weights, so it always returns the optimal "
     "route; the implementation deliberately uses a linear scan to pick the "
     "cheapest unsettled node, which is simple and fast for the ten-to-fifty "
     "node networks this tool targets. For triage, the hand-written max-heap "
     "gives the dashboard its most-urgent-first ordering in O(log n) per "
     "operation and was written from scratch to demonstrate heap mechanics "
     "rather than delegating to a library. BFS was the right tool for "
     "reachability because the question (what is still connected?) has no "
     "weights, and it also powers the critical-road warning that prevents a "
     "coordinator from accidentally cutting off part of the network.")
para("The final iteration's user-facing changes came directly from observed "
     "problems: early map versions drew markers at wrong positions when a data "
     "file predated map coordinates, which the migration and bounds-fitting "
     "logic now handles and which the map render test verifies; and early "
     "layouts clipped labels at smaller window sizes, which motivated the "
     "automated overlap audit that now runs as part of the test suite.")
h2("6.4 LIMITATIONS")
bullet("The application is single-user and desktop-bound; there is no "
       "concurrency handling, no networked multi-operator mode, and no "
       "load testing.")
bullet("The map is a schematic simulation: coordinates are logical positions "
       "on a virtual canvas, not real GIS coordinates, and no external map "
       "service is used.")
bullet("Storage uses Java serialization rather than a SQL database, which is "
       "simple and dependency-free but does not support ad-hoc queries or "
       "concurrent access.")
bullet("Code coverage and response time were not measured with tools; test "
       "counts come from the custom harnesses instead of JUnit.")
bullet("The demo dataset is small (8 citizens, 10 locations, 13 roads by "
       "default); behaviour with very large networks was not tested.")
bullet("Some usability aspects - typing feel, dialog sizing on unusual DPI "
       "settings - were checked manually and may still require verification on "
       "other machines.")
page_break()

# ================= CHAPTER 7 =================
chapter_heading("CHAPTER 7", "TEAM REFLECTION AND LEARNING OUTCOMES")
para("")
h2("7.1 INDIVIDUAL REFLECTIONS")
para("[Student 1 Name]: I worked mainly on the backend - the model classes, "
     "the services, validation and the serialization-based storage. The biggest "
     "thing I learned was how a layered design pays off: once DataManager "
     "became the single owner of data with change listeners, adding new "
     "modules stopped breaking existing ones. My main challenge was designing "
     "the save-on-every-change mechanism so that a corrupted write could never "
     "destroy the previous state; the .bak backup file was the solution I was "
     "proudest of.")
para("[Student 2 Name]: I focused on the algorithms and the map. Implementing "
     "Dijkstra by hand - including the blocked-road rule and the distance/time "
     "weight switch - taught me why the algorithm needs non-negative weights, "
     "and writing the max-heap myself made the sift-up and sift-down mechanics "
     "concrete in a way textbooks did not. The hardest bug was the map drawing "
     "all markers at one point when an old data file lacked coordinates; "
     "debugging it with a pixel-level test taught me to verify rendering "
     "programmatically instead of by eye.")
para("[Student 3 Name]: I built most of the Swing interface - the sidebar, "
     "dashboard, tables and dialogs - and the automated layout audit. I "
     "learned how Swing layout managers compose (BorderLayout inside "
     "GridBagLayout inside scroll panes) and that clipping problems must be "
     "fixed in the layout, not by shrinking labels. My challenge was keeping "
     "twelve screens consistent; extracting shared helpers into UiUtil and "
     "Theme was what made the interface coherent.")
h2("7.2 TEAM LEARNING")
para("Dividing work by layer (data, algorithms, interface) worked well because "
     "each layer had a clear contract, and the weekly mentor reviews forced "
     "integration to happen early instead of at the end. Decisions were made "
     "by quickly prototyping both options and comparing them against the "
     "offline, zero-dependency requirement - the choice of serialization over "
     "a database in Chapter 2 is the clearest example. If we restarted the "
     "cycle, we would write the automated tests from the first week instead of "
     "the eleventh: the harnesses caught several regressions in the final "
     "iterations, and having them earlier would have saved rework. Mentor "
     "feedback also changed our direction twice: we added the capacity and "
     "stock guards after the second review, and we invested in the map's "
     "readability (badges, zoom, blocked-road styling) after the mid-term demo "
     "showed that the routing feature only convinces an audience when the map "
     "makes it visible.")
h2("7.3 COURSE OUTCOMES - EVIDENCE SUMMARY")
bullet("Applied object-oriented concepts (classes, encapsulation, "
       "inheritance through the Person base class and Prioritizable interface, "
       "polymorphism in the undo stack) across 60+ classes.",
       bold_prefix="OOP design: ")
bullet("Used ArrayList, HashMap, LinkedList, PriorityQueue, custom heap, "
       "stack and queues - each justified by a specific need (Section 2.2 and "
       "Chapter 4).",
       bold_prefix="Data structures: ")
bullet("Implemented Dijkstra, BFS, DFS and greedy allocation and wired them "
       "to visible features (route highlighting, impact analysis, alternatives, "
       "allocation plan).",
       bold_prefix="Algorithms: ")
bullet("Java object serialization with backup and recovery replaces a "
       "database server while keeping data permanent across restarts.",
       bold_prefix="File handling: ")
bullet("Swing interface with twelve modules, custom 2D map painting, and "
       "input validation with user-readable error dialogs.",
       bold_prefix="GUI programming: ")
bullet("Weekly log (Table 3.1), layered task division, and mentor-guided "
       "iterations documented in Chapter 4.",
       bold_prefix="Teamwork and process: ")
bullet("66 automated checks written and passing; failures found by the suites "
       "were fixed before the final build.",
       bold_prefix="Testing discipline: ")
page_break()

# ================= CHAPTER 8 =================
chapter_heading("CHAPTER 8", "CONCLUSION AND FUTURE SCOPE")
para("")
h2("8.1 CONCLUSION")
para("The Smart Disaster Response System fulfils the objective set out in "
     "Chapter 1: a complete, offline, Java-based desktop application that "
     "manages the disaster-response workflow from citizen registration to "
     "evacuation routing. The final application provides twelve connected "
     "modules backed by a layered object-oriented design, permanent "
     "serialization-based storage with automatic backup, and a simulated "
     "interactive map that makes the road graph and the computed routes "
     "visible. Dijkstra's algorithm drives evacuation routing, a hand-written "
     "binary max-heap drives disaster triage, and BFS, DFS and greedy "
     "allocation support network analysis and resource planning. The build "
     "passes 66 automated checks covering workflows, validation, persistence, "
     "rendering and layout. Beyond the artefact, the project demonstrated to "
     "the team how core Java concepts - collections, exception handling, file "
     "I/O, Swing and algorithm implementation - combine into a tool that solves "
     "a real coordination problem.")
h2("8.2 FUTURE SCOPE")
bullet("Replace serialization with an embedded SQL database (for example "
       "SQLite or MySQL) to support ad-hoc reporting and larger datasets.")
bullet("Add multi-user support with roles and a shared server so several "
       "operators can work on the same incident simultaneously.")
bullet("Enhance the map with pan-and-zoom memory, real geographic coordinates "
       "import, and GPS-based citizen positioning.")
bullet("Introduce automatic alerting (SMS or e-mail) to citizens and volunteers "
       "when a disaster is reported in their area.")
bullet("Export incident summary reports to PDF or Excel for post-disaster "
       "review and audit.")
bullet("Extend pathfinding to avoid passing through active disaster zones and "
       "to balance load across multiple shelters.")
page_break()

# ================= REFERENCES =================
center("REFERENCES", bold=True, size=16)
para("")
refs = [
    '[1] Oracle Corporation, "The Java\u2122 Tutorials," '
    'https://docs.oracle.com/javase/tutorial/ (accessed September 2026).',
    '[2] Oracle Corporation, "Java SE API Specification - java.util and '
    'java.util.concurrent packages," https://docs.oracle.com/en/java/',
    '[3] Oracle Corporation, "Java Object Serialization Specification," '
    'https://docs.oracle.com/en/java/javase/serialization/',
    '[4] E. W. Dijkstra, "A Note on Two Problems in Connexion with Graphs," '
    'Numerische Mathematik, vol. 1, no. 1, pp. 269-271, 1959.',
    '[5] T. H. Cormen, C. E. Leiserson, R. L. Rivest and C. Stein, '
    'Introduction to Algorithms, 3rd ed., MIT Press, 2009 (breadth-first and '
    'depth-first search, heaps, greedy algorithms).',
    '[6] C. S. Horstmann, Core Java Volume I - Fundamentals, 12th ed., '
    'Pearson, 2021.',
    '[7] Spring Corporation, "Spring Boot Reference Documentation," '
    'https://spring.io/projects/spring-boot (accessed September 2026).',
    '[8] Oracle Corporation, "Class JComponent and the Swing package," '
    'Java SE API Specification, https://docs.oracle.com/en/java/',
]
for ref in refs:
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p.paragraph_format.line_spacing_rule = WD_LINE_SPACING.DOUBLE
    p.paragraph_format.left_indent = Inches(0.5)
    p.paragraph_format.first_line_indent = Inches(-0.5)
    style_run(p.add_run(ref))
page_break()

# ================= APPENDIX =================
center("APPENDIX", bold=True, size=16)
para("")
h2("A.1 FULL SOURCE CODE")
para("The complete Java source code of the Smart Disaster Response System "
     "(approximately 70 classes across the model, algorithm, service, data, "
     "util, ui and test packages) is submitted along with this report in the "
     "project folder SDRS/. The folder contains compile.bat, run.bat and "
     "test.bat scripts, so the application can be compiled, launched and "
     "tested with a single double-click on any machine with a Java "
     "installation. The source is also organised for review as follows: "
     "src/sdrs/model (entity classes), src/sdrs/algorithm (Graph, RouteFinder, "
     "DisasterPriorityQueue, SortSearch), src/sdrs/service (business rules and "
     "data management), src/sdrs/data (SystemData and FileDataStore), "
     "src/sdrs/ui (interface, panels, dialogs, map canvas) and src/sdrs/test "
     "(automated test harnesses).")
h2("A.2 COMPLETE WEEKLY PBL LOG AND MENTOR SIGN-OFFS")
para("The weekly progress is fully captured in Table 3.1 in Chapter 3; each "
     "phase was reviewed and signed off by the mentor at the weekly review "
     "meeting. No additional log entries fall outside that table.")
h2("A.3 SELF AND PEER ASSESSMENT")
para("", space_after=4)
make_table([
    ("Team Member", "Self-Rated Contribution", "Peer-Rated Contribution", "Remarks"),
    ("[Student 1 Name]", "[%]", "[%]", "Backend design, services, persistence"),
    ("[Student 2 Name]", "[%]", "[%]", "Algorithms, map, routing features"),
    ("[Student 3 Name]", "[%]", "[%]", "Interface, dialogs, testing suites"),
])

doc.save("PBL_Report_SDRS.docx")
print("Saved PBL_Report_SDRS.docx")
