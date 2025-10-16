# Healthcare Management System - Project Report

## Page 1: Technology Challenges and Improvements

### Current Technology Stack
We built this healthcare system using JavaFX, MySQL, and Maven. While these are solid choices, we ran into some real challenges that made us think about better alternatives.

### The Problems We Faced

**JavaFX Limitations**
- JavaFX is getting less support from Oracle, which worries us about long-term maintenance
- The UI feels a bit outdated compared to modern web applications
- Cross-platform deployment is tricky - what works on Mac might break on Windows
- Mobile support is basically non-existent, which is a big problem for healthcare workers who need to move around

**Database Connection Issues**
- MySQL is powerful but can be overkill for smaller healthcare facilities
- Setting up the database requires technical knowledge that not all healthcare staff have
- Backup and recovery procedures are complex
- Real-time synchronization between multiple locations is challenging

**Testing Challenges**
- Mockito had compatibility issues with newer Java versions (Java 24)
- Database testing required complex setup with H2 in-memory databases
- Integration testing was harder than expected due to database dependencies

### Better Technology Alternatives

**For the Frontend:**
- **React with TypeScript**: Modern, responsive, works on any device
- **Flutter**: Single codebase for web, mobile, and desktop
- **Vue.js**: Easier to learn, great for rapid development
- **Progressive Web App (PWA)**: Works offline, installs like a native app

**For the Backend:**
- **Spring Boot**: More robust than plain Java, better security, easier testing
- **Node.js with Express**: Faster development, huge community support
- **Python with Django**: Great for data analysis, machine learning integration
- **Microservices Architecture**: Break the system into smaller, manageable pieces

**For the Database:**
- **PostgreSQL**: Better for complex queries, more features
- **MongoDB**: Easier to scale, better for document-based data
- **Cloud Databases (AWS RDS, Google Cloud SQL)**: No setup required, automatic backups
- **Firebase**: Real-time updates, built-in authentication

**For Testing:**
- **Cypress**: Better for end-to-end testing
- **Jest**: More modern testing framework
- **Docker**: Consistent testing environments
- **GitHub Actions**: Automated testing on every code change

## Page 2: Design Decisions and Lessons Learned

### Why We Made These Choices

**Database Design Decisions**
We chose a relational database structure because healthcare data has lots of connections - patients have beds, staff have shifts, medications have prescriptions. This made sense at the time, but we learned that sometimes simpler is better.

**User Interface Approach**
We went with separate dashboards for different roles (Manager, Doctor, Nurse) because each role needs different information. This was the right call - it keeps things organized and reduces confusion.

**Testing Strategy**
We created both unit tests (testing individual pieces) and integration tests (testing everything together). This dual approach caught more bugs and gave us confidence that the system actually works.

### What We Learned

**Start Simple, Then Add Complexity**
We tried to build everything at once, which made debugging harder. Next time, we'd start with just patient management, then add medications, then add scheduling.

**User Feedback is Crucial**
We built what we thought healthcare workers needed, but we should have talked to actual nurses and doctors first. They know their workflow better than we do.

**Security Should Come First**
Healthcare data is sensitive. We should have planned security from day one, not added it later. Things like encryption, audit logs, and access controls need to be built-in, not bolted on.



### What We'd Do Differently

**Better Planning Phase**
- Talk to real healthcare workers before writing any code
- Create wireframes and get feedback before building



**Better User Experience**
- Make the interface more intuitive - healthcare workers are busy
- Add search functionality everywhere
- Make common tasks faster with shortcuts and templates
- Design for one-handed use on tablets

### The Big Picture

Building a healthcare management system taught us that technology is just a tool. The real challenge is understanding how healthcare actually works and building something that makes people's jobs easier, not harder.

The best technology choice is the one that solves real problems for real people. Sometimes that means choosing simpler tools that work reliably over complex ones that look impressive but break when you need them most.

Our next version would focus on being useful, reliable, and easy to use rather than having every possible feature. Sometimes less really is more.
