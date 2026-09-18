import {
  AppBar,
  Button,
  Container,
  Stack,
  Toolbar,
  Typography,
} from "@mui/material";
import { NavLink } from "react-router";

const navStyle = {
  "&.active": {
    color: "primary.main",
    fontWeight: 800,
    borderBottom: 2,
    borderColor: "primary.main",
    borderRadius: 0,
  },
};

export function Navbar() {
  return (
    <AppBar position="static" color="transparent" elevation={0}>
      <Container maxWidth="xl">
        <Toolbar disableGutters>
          <Typography
            variant="h6"
            fontWeight={800}
            color="primary"
            sx={{ flexGrow: 1 }}
          >
            DataPulse
          </Typography>
          <Stack direction="row" spacing={1}>
            <Button
              component={NavLink}
              to="/dashboard"
              className="nav-link"
              sx={navStyle}
            >
              Dashboard
            </Button>
            <Button
              component={NavLink}
              to="/ingestions"
              end
              className="nav-link"
              sx={navStyle}
            >
              Jobs processados
            </Button>
            <Button
              component={NavLink}
              to="/ingestions/new"
              className="nav-link"
              sx={navStyle}
            >
              Nova ingestão
            </Button>
          </Stack>
        </Toolbar>
      </Container>
    </AppBar>
  );
}
