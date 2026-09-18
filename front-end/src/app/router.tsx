import { createBrowserRouter, redirect } from "react-router";
import { AppLayout } from "../layouts/AppLayout";
import { DashboardPage } from "../pages/DashboardPage";
import { IngestionJobsPage } from "../pages/IngestionJobsPage";
import { IngestionStatusPage } from "../pages/IngestionStatusPage";
import { NewIngestionPage } from "../pages/NewIngestionPage";
import { NotFoundPage } from "../pages/NotFoundPage";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: AppLayout,
    children: [
      { index: true, loader: () => redirect("/dashboard") },
      { path: "dashboard", Component: DashboardPage },
      { path: "ingestions", Component: IngestionJobsPage },
      { path: "ingestions/new", Component: NewIngestionPage },
      { path: "ingestions/:jobId", Component: IngestionStatusPage },
      { path: "*", Component: NotFoundPage },
    ],
  },
]);
