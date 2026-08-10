# Enterprise UX

## Why this matters

Enterprise applications serve people doing consequential work repeatedly. They must support complexity without making users feel overwhelmed or unsafe.

## Core concepts

- **Role-based experiences:** Permissions and responsibilities change what different users can see and do.
- **Data-dense interfaces:** Tables, dashboards, filters, saved views, and bulk actions support efficient recurring work.
- **Workflow integrity:** Approvals, audit history, status, ownership, and exception handling make work accountable.
- **Operational efficiency:** Minimize repetitive steps while preserving clarity and confidence.
- **Domain complexity:** Learn the business vocabulary, rules, dependencies, and risk before simplifying the interface.

## Think through

1. Which roles perform which tasks, and where do their responsibilities overlap?
2. Which information is needed to decide, act, and verify completion?
3. What needs an audit trail, confirmation, or permission check?
4. Which actions are frequent enough to optimize with shortcuts, bulk actions, or saved views?
5. How do users handle exceptions, blocked work, and incomplete data?

## Practice

Design an approval queue for expenses, claims, or purchase requests. Include prioritization, filters, assignment, decision rationale, and audit history.

## Deliverable

Create a role-and-permission matrix, workflow diagram, and annotated queue screen showing how the design supports safe, efficient work.

## Common interview questions

### What is different about designing enterprise software?

Enterprise users often perform complex, repeated, high-consequence tasks and have different permissions or responsibilities. I focus on efficiency, clarity, data quality, auditability, error recovery, and role-specific workflows. The right experience may be denser than a consumer app, but its complexity should be organized around the work users need to complete.

### How do you simplify a complex workflow?

I learn the underlying domain and identify the decisions, rules, exceptions, and handoffs that are truly necessary. Then I separate frequent work from rare exceptions, sequence information around the user's task, automate safe defaults, and preserve visibility into status and consequences. I validate that simplification does not remove control or compliance needs.

## Key terms / cheat sheet

- **Role-based access control:** Permissions based on a user's assigned role.
- **Audit trail:** A record of actions, decisions, and changes.
- **Bulk action:** An operation applied to multiple selected records.

## Scenario challenge

Design a claims-review queue where reviewers have different approval limits, must record decisions, and need to handle incomplete submissions.

## Common pitfalls

- Simplifying by hiding important status, history, or control.
- Treating all roles as if they need the same data and actions.
- Optimizing bulk actions without safeguards against costly mistakes.

## Review checklist

- [ ] Roles, permissions, handoffs, and accountability are visible.
- [ ] Frequent work is efficient while exceptions remain recoverable.
- [ ] The design supports auditability and accurate status.

## Portfolio evidence

Show the domain model, role matrix, workflow map, queue design, and rationale for safety and efficiency tradeoffs.

## Flashcards

**Q:** Why are audit trails important in enterprise UX?  
**A:** They support accountability, troubleshooting, compliance, and user confidence in consequential workflows.

**Q:** What should role-based design change?  
**A:** The information, actions, and workflows a person needs to perform their responsibilities.

## Resources

- [Nielsen Norman Group: Enterprise UX](https://www.nngroup.com/articles/enterprise-ux/)
- [GOV.UK: Designing for Different Users](https://www.gov.uk/service-manual/user-research/user-research-for-government-services)

## Reflection log

- Which enterprise rule is essential, and which merely reflects legacy process?
- Where could a safe default reduce repetitive work?
