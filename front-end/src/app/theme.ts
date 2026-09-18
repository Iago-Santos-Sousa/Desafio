import { createTheme } from "@mui/material/styles";

export const theme = createTheme({
  palette: {
    primary: { main: "#4f46e5" },
    background: { default: "#f8fafc" },
  },
  typography: { fontFamily: "Inter, ui-sans-serif, system-ui, sans-serif" },
  shape: { borderRadius: 12 },
});
