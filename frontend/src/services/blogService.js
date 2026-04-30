const posts = [
  {
    id: 1,
    title: "How We Build Reliable Order Processing",
    description: "A quick look at REST orchestration and event-driven status updates.",
    date: "2026-04-20",
    content:
      "Our checkout flow combines synchronous REST for immediate business commands and Kafka events for asynchronous updates. This hybrid approach gives users instant feedback while keeping downstream services decoupled."
  },
  {
    id: 2,
    title: "Designing Inventory for Scale",
    description: "Patterns to reserve, release, and reconcile stock safely.",
    date: "2026-04-24",
    content:
      "Inventory management is a consistency problem first. Reserve-before-pay and release-on-failure are the two core steps that reduce overselling risk and support clear compensation behavior."
  },
  {
    id: 3,
    title: "Improving Payment Transparency",
    description: "Making payment outcomes visible across services.",
    date: "2026-04-28",
    content:
      "Payment status must be tracked beyond the immediate API response. By publishing order status events, services can react asynchronously and build observability dashboards without tight coupling."
  }
];

export const getBlogPosts = async () => posts;
export const getBlogPostById = async (id) => posts.find((post) => post.id === Number(id)) || null;
