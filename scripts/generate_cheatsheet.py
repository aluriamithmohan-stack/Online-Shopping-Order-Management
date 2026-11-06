import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

def set_cell_background(cell, fill_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:val'), 'clear')
    shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'), fill_hex)
    tcPr.append(shd)

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
        node = OxmlElement(f'w:{m}')
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def create_document():
    doc = docx.Document()

    # Page Margins (1 inch all around)
    for section in doc.sections:
        section.top_margin = Inches(1)
        section.bottom_margin = Inches(1)
        section.left_margin = Inches(1)
        section.right_margin = Inches(1)

    # Base Styles
    normal_style = doc.styles['Normal']
    normal_style.font.name = 'Calibri'
    normal_style.font.size = Pt(11)
    normal_style.font.color.rgb = RGBColor(0x33, 0x33, 0x33)

    # Document Header / Title
    title_p = doc.add_paragraph()
    title_p.paragraph_format.space_before = Pt(0)
    title_p.paragraph_format.space_after = Pt(4)
    run_title = title_p.add_run("DevOps & Testing Commands Reference Manual")
    run_title.font.name = 'Calibri'
    run_title.font.size = Pt(24)
    run_title.font.bold = True
    run_title.font.color.rgb = RGBColor(0x1B, 0x36, 0x5D) # Deep Navy

    sub_p = doc.add_paragraph()
    sub_p.paragraph_format.space_after = Pt(18)
    run_sub = sub_p.add_run("Project: Online Shopping & Order Management System (ShopEase)\nDevOps Practical Lab Guide • Git, Docker, Jenkins & Test Automation")
    run_sub.font.name = 'Calibri'
    run_sub.font.size = Pt(12)
    run_sub.font.italic = True
    run_sub.font.color.rgb = RGBColor(0x55, 0x55, 0x55)

    def add_section_heading(title):
        h = doc.add_paragraph()
        h.paragraph_format.space_before = Pt(18)
        h.paragraph_format.space_after = Pt(6)
        h.paragraph_format.keep_with_next = True
        r = h.add_run(title)
        r.font.name = 'Calibri'
        r.font.size = Pt(15)
        r.font.bold = True
        r.font.color.rgb = RGBColor(0x00, 0x5A, 0x9E) # Blue
        return h

    def add_code_box(cmd_text, description=None):
        if description:
            dp = doc.add_paragraph()
            dp.paragraph_format.space_before = Pt(4)
            dp.paragraph_format.space_after = Pt(2)
            dr = dp.add_run(f"• {description}:")
            dr.font.bold = True
            dr.font.size = Pt(10.5)

        tbl = doc.add_table(rows=1, cols=1)
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        cell = tbl.cell(0, 0)
        set_cell_background(cell, "F4F6F9")
        set_cell_margins(cell, top=100, bottom=100, left=150, right=150)
        
        cp = cell.paragraphs[0]
        cp.paragraph_format.space_before = Pt(0)
        cp.paragraph_format.space_after = Pt(0)
        cr = cp.add_run(cmd_text)
        cr.font.name = 'Consolas'
        cr.font.size = Pt(10)
        cr.font.color.rgb = RGBColor(0x24, 0x29, 0x2E)

        # Space after table
        sp = doc.add_paragraph()
        sp.paragraph_format.space_before = Pt(0)
        sp.paragraph_format.space_after = Pt(4)

    # ==========================================
    # SECTION 1: GIT COMMANDS
    # ==========================================
    add_section_heading("1. Git Commands Reference")
    
    p = doc.add_paragraph("Essential Git commands for version control, repository synchronization, and pushing changes to GitHub.")
    p.paragraph_format.space_after = Pt(8)

    add_code_box("git status", "Check repository status (modified/staged/untracked files)")
    add_code_box("git add .\n# or stage a specific file:\ngit add Jenkinsfile", "Stage changes to index")
    add_code_box('git commit -m "feat: your descriptive commit message here"', "Commit staged changes")
    add_code_box("git push origin main", "Push committed changes to remote GitHub repository")
    add_code_box("git pull origin main", "Pull latest changes from remote repository")
    add_code_box("git remote -v", "Verify connected remote repository URL")
    add_code_box("git log --oneline -n 5", "View recent commit history")
    add_code_box("git restore <filename>\n# or discard all unstaged changes:\ngit restore .", "Discard local uncommitted changes")

    # ==========================================
    # SECTION 2: TEST CASES (INDIVIDUAL & SUITE)
    # ==========================================
    add_section_heading("2. Test Automation Commands (Functional & Selenium)")
    
    p2 = doc.add_paragraph("The test suite consists of 13 API/Functional tests and the 8 Selenium WebDriver end-to-end test cases (TC01 to TC08). Each test case can be executed individually or as a complete suite.")
    p2.paragraph_format.space_after = Pt(8)

    # Table for Individual Selenium Tests
    table = doc.add_table(rows=1, cols=4)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False

    headers = ["Test ID", "Test Description", "Quick npm Command", "Direct Maven Command"]
    hdr_cells = table.rows[0].cells
    for i, h in enumerate(headers):
        hdr_cells[i].text = h
        set_cell_background(hdr_cells[i], "1B365D")
        p = hdr_cells[i].paragraphs[0]
        p.runs[0].font.name = 'Calibri'
        p.runs[0].font.bold = True
        p.runs[0].font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
        p.runs[0].font.size = Pt(10)

    test_data = [
        ("TC01", "Customer Registration", "npm run test:tc01", "mvn -f selenium-tests/pom.xml test -Dtest=TC01_CustomerRegistrationTest"),
        ("TC02", "Product Catalogue", "npm run test:tc02", "mvn -f selenium-tests/pom.xml test -Dtest=TC02_ProductCatalogueTest"),
        ("TC03", "Product Search", "npm run test:tc03", "mvn -f selenium-tests/pom.xml test -Dtest=TC03_ProductSearchTest"),
        ("TC04", "Add to Cart", "npm run test:tc04", "mvn -f selenium-tests/pom.xml test -Dtest=TC04_AddToCartTest"),
        ("TC05", "Update Cart Quantity", "npm run test:tc05", "mvn -f selenium-tests/pom.xml test -Dtest=TC05_UpdateCartQuantityTest"),
        ("TC06", "Checkout", "npm run test:tc06", "mvn -f selenium-tests/pom.xml test -Dtest=TC06_CheckoutTest"),
        ("TC07", "Order History", "npm run test:tc07", "mvn -f selenium-tests/pom.xml test -Dtest=TC07_OrderHistoryTest"),
        ("TC08", "Admin Product Management", "npm run test:tc08", "mvn -f selenium-tests/pom.xml test -Dtest=TC08_AdminProductManagementTest"),
    ]

    for row_idx, data in enumerate(test_data):
        row_cells = table.add_row().cells
        bg = "FFFFFF" if row_idx % 2 == 0 else "F9FAFB"
        for col_idx, text in enumerate(data):
            row_cells[col_idx].text = text
            set_cell_background(row_cells[col_idx], bg)
            set_cell_margins(row_cells[col_idx], top=80, bottom=80, left=100, right=100)
            p = row_cells[col_idx].paragraphs[0]
            p.runs[0].font.size = Pt(9.5)
            if col_idx in [2, 3]:
                p.runs[0].font.name = 'Consolas'
                p.runs[0].font.size = Pt(8.5)

    doc.add_paragraph().paragraph_format.space_after = Pt(6)

    add_code_box("npm run test:selenium", "Run Complete Selenium Test Suite (All 7 Cases)")
    add_code_box("npm test", "Run Functional API Test Suite (All 13 Cases - Auto starts/stops server)")
    add_code_box("npm run dev\n# or\nnpm start", "Start Application Locally (http://localhost:3000)")

    # ==========================================
    # SECTION 3: DOCKER COMMANDS
    # ==========================================
    add_section_heading("3. Docker Commands Reference")

    p3 = doc.add_paragraph("Commands for containerizing, executing, inspecting, and managing the ShopEase web application using Docker.")
    p3.paragraph_format.space_after = Pt(8)

    add_code_box("docker build -t shopping-system:latest .", "Build Docker Image from Dockerfile")
    add_code_box("docker images", "List Available Local Docker Images")
    add_code_box("docker run -d -p 3000:3000 --name shopease-app shopping-system:latest", "Run Container in Background (Port 3000 mapped)")
    add_code_box("docker ps", "List Active/Running Containers")
    add_code_box("docker ps -a", "List All Containers (Including Stopped Ones)")
    add_code_box("docker logs -f shopease-app", "Inspect Container Real-Time Logs")
    add_code_box("docker stop shopease-app", "Stop Running Container")
    add_code_box("docker rm shopease-app", "Remove Container")
    add_code_box("docker rmi shopping-system:latest", "Remove Docker Image")
    add_code_box("docker system prune -f", "Clean Up Unused Containers, Networks & Dangling Images")

    # ==========================================
    # SECTION 4: JENKINS COMMANDS & PIPELINE
    # ==========================================
    add_section_heading("4. Jenkins CI/CD Pipeline Reference")

    p4 = doc.add_paragraph("Commands and step-by-step procedures for managing and executing the Jenkins CI/CD pipeline.")
    p4.paragraph_format.space_after = Pt(8)

    add_code_box("java -jar jenkins.war --httpPort=8080", "Start Jenkins Server Manually (WAR file execution)")
    add_code_box("http://localhost:8080", "Access Jenkins Web Interface")

    p_pipe = doc.add_paragraph("Step-by-Step Jenkins Job Setup:")
    p_pipe.paragraph_format.space_before = Pt(6)
    p_pipe.paragraph_format.space_after = Pt(4)

    steps = [
        "1. Open Jenkins Dashboard at http://localhost:8080.",
        "2. Click 'New Item', enter name 'Online-Shopping-Pipeline', select 'Pipeline', and click 'OK'.",
        "3. In the Pipeline section at the bottom, change Definition to 'Pipeline script from SCM'.",
        "4. Set SCM to 'Git'.",
        "5. Enter Repository URL: https://github.com/aluriamithmohan-stack/Online-Shopping-Order-Management",
        "6. Set Branch Specifier to '*/main'.",
        "7. Ensure Script Path is set to 'Jenkinsfile'.",
        "8. Click 'Save' and then click 'Build Now' to execute the CI/CD pipeline."
    ]
    for s in steps:
        sp = doc.add_paragraph(s)
        sp.paragraph_format.space_before = Pt(0)
        sp.paragraph_format.space_after = Pt(2)
        sp.runs[0].font.size = Pt(10)

    doc.add_paragraph().paragraph_format.space_after = Pt(4)

    p_stages = doc.add_paragraph("Jenkins Pipeline Stages Summary:")
    p_stages.paragraph_format.space_before = Pt(6)
    p_stages.paragraph_format.space_after = Pt(4)
    p_stages.runs[0].font.bold = True

    stage_items = [
        ("Stage 1: Checkout SCM", "Clones latest commit from GitHub repository."),
        ("Stage 2: Install Dependencies", "Executes 'npm install' to ensure packages are ready."),
        ("Stage 3: Run Functional Tests", "Executes 'npm test' to verify API routes and database transactions."),
        ("Stage 4: Run Selenium Automated Tests", "Executes 'npm run test:selenium' (TC01 to TC07 end-to-end tests)."),
        ("Stage 5: Docker Build & Tag", "Executes 'docker build' and tags the container image with build number.")
    ]
    for name, desc in stage_items:
        sp = doc.add_paragraph()
        sp.paragraph_format.space_before = Pt(0)
        sp.paragraph_format.space_after = Pt(2)
        r1 = sp.add_run(f"• {name}: ")
        r1.font.bold = True
        r1.font.size = Pt(10)
        r2 = sp.add_run(desc)
        r2.font.size = Pt(10)

    # Save Document
    doc_path = "DevOps_and_Testing_Commands_CheatSheet.docx"
    doc.save(doc_path)
    print(f"Document saved successfully as: {doc_path}")

if __name__ == "__main__":
    create_document()
