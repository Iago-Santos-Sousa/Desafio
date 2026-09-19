import { Box, Container } from "@mui/material";
import { Outlet } from "react-router";
import { Navbar } from "../components/Navbar";

export function AppLayout() {
  return (
    <Box className="min-h-screen bg-app-background">
      <Navbar />
      <Container maxWidth="xl" className="py-8 md:py-10">
        <Outlet />
      </Container>
    </Box>
  );
}
