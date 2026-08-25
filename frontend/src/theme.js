/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import { createTheme } from '@mui/material/styles';
import { colors, fontFamily, shadows, SPACING_UNIT_PX } from './theme/tokens';

const theme = createTheme({
  // theme.spacing(n) then reproduces --space-n from the design tokens
  // (1=4px, 2=8px, 3=12px, 4=16px, 6=24px, 8=32px) exactly.
  spacing: SPACING_UNIT_PX,
  palette: {
    mode: 'light',
    primary: {
      main: colors.accent,
      light: colors.accentRamp[400],
      dark: colors.accentRamp[700],
      contrastText: colors.bg,
    },
    secondary: {
      // The design system's "accent-2" is a mono variant of the same hue,
      // not a distinct semantic color (see _ds readme) — treat it as accent.
      main: colors.accent,
      contrastText: colors.bg,
    },
    // Status meaning is carried by the status-vocabulary module (Phase 2),
    // not by palette color — green/amber/blue are retired. These three
    // stay within the ink/accent tokens for the few MUI components
    // (Alert, form validation) that need a distinct-but-unranked signal.
    success: { main: colors.neutral[800], contrastText: colors.bg },
    warning: { main: colors.accentRamp[600], contrastText: colors.bg },
    error: { main: colors.accentRamp[700], contrastText: colors.bg },
    info: { main: colors.neutral[700], contrastText: colors.bg },
    background: {
      default: colors.bg,
      paper: colors.surface,
    },
    text: {
      primary: colors.text,
      secondary: colors.textMuted,
    },
    divider: colors.divider,
  },
  shape: {
    // Zero everywhere, deliberately — the single easiest rule to break by
    // accident in this system. Buttons, inputs, tags, dialogs, cards, avatars.
    borderRadius: 0,
  },
  typography: {
    fontFamily,
    h1: { fontFamily, fontWeight: 800, fontSize: 42, lineHeight: 1.12, letterSpacing: '-0.015em' },
    h2: { fontFamily, fontWeight: 800, fontSize: 32, lineHeight: 1.12, letterSpacing: '-0.015em' },
    h3: { fontFamily, fontWeight: 800, fontSize: 25, lineHeight: 1.12, letterSpacing: '-0.015em' },
    h4: { fontFamily, fontWeight: 800, fontSize: 20, lineHeight: 1.15 },
    h5: { fontFamily, fontWeight: 800, fontSize: 16, lineHeight: 1.2 },
    h6: {
      fontFamily,
      fontWeight: 800,
      fontSize: 13,
      letterSpacing: '0.08em',
      textTransform: 'uppercase',
    },
    subtitle1: { fontFamily, fontWeight: 600, fontSize: 15 },
    subtitle2: { fontFamily, fontWeight: 600, fontSize: 13 },
    body1: { fontSize: 15, lineHeight: 1.55 },
    body2: { fontSize: 13, lineHeight: 1.5 },
    caption: { fontSize: 11, letterSpacing: '0.02em' },
    overline: {
      fontSize: 11,
      fontWeight: 600,
      letterSpacing: '0.08em',
      textTransform: 'uppercase',
    },
    button: { fontFamily, fontWeight: 800, fontSize: 14, textTransform: 'none' },
  },
  shadows: [
    'none',
    shadows.sm,
    shadows.sm,
    shadows.md,
    shadows.md,
    shadows.md,
    shadows.lg,
    shadows.lg,
    ...Array(18).fill(shadows.lg),
  ],
  transitions: {
    // Motion budget: 120ms for state changes on the touched element, 180ms
    // ease-out for entering surfaces (dialog, menu, toast). MUI's own
    // Dialog/Snackbar/Menu/Fade/Grow transitions all read from these.
    duration: {
      shortest: 120,
      shorter: 120,
      short: 150,
      standard: 180,
      complex: 180,
      enteringScreen: 180,
      leavingScreen: 150,
    },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: { backgroundColor: colors.bg },
        // Focus is word-first everywhere — a visible accent outline on every
        // interactive element, never the default blue browser ring.
        '*:focus-visible': {
          outline: `2px solid ${colors.accent}`,
          outlineOffset: '2px',
        },
        '@media (prefers-reduced-motion: reduce)': {
          '*, *::before, *::after': {
            animationDuration: '0.01ms !important',
            animationIterationCount: '1 !important',
            transitionDuration: '0.01ms !important',
            scrollBehavior: 'auto !important',
          },
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 0,
          fontWeight: 800,
          paddingInline: 14,
          boxShadow: 'none',
        },
        contained: {
          boxShadow: 'none',
          '&:hover': { boxShadow: 'none' },
        },
        outlined: {
          borderColor: colors.divider,
          '&:hover': { borderColor: colors.text, backgroundColor: 'rgba(32, 30, 29, 0.07)' },
        },
        text: {
          color: colors.accent,
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: { backgroundImage: 'none', borderRadius: 0 },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          boxShadow: 'none',
          borderRadius: 0,
          border: `1px solid ${colors.divider}`,
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          boxShadow: 'none',
          borderBottom: `2px solid ${colors.divider}`,
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: colors.surface,
          backgroundImage: 'none',
          borderRadius: 0,
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { borderRadius: 0, fontWeight: 600 },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: { borderRadius: 0, boxShadow: shadows.lg },
      },
    },
    MuiTextField: {
      defaultProps: { variant: 'outlined' },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 0,
          backgroundColor: colors.surface,
        },
        notchedOutline: {
          borderColor: colors.divider,
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: { borderBottom: `1px solid ${colors.divider}` },
        head: {
          fontSize: 11,
          fontWeight: 600,
          letterSpacing: '0.08em',
          textTransform: 'uppercase',
          color: colors.textMuted,
          borderBottom: `2px solid ${colors.divider}`,
        },
      },
    },
    MuiAvatar: {
      styleOverrides: {
        root: { borderRadius: 0 },
      },
    },
    MuiAlert: {
      styleOverrides: {
        root: { borderRadius: 0 },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: { borderRadius: 0 },
      },
    },
    MuiSkeleton: {
      defaultProps: { animation: 'pulse' },
      styleOverrides: {
        root: { backgroundColor: colors.neutral[300], borderRadius: 0, transform: 'none' },
      },
    },
  },
});

export default theme;
