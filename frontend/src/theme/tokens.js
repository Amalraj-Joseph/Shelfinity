/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */

// Design tokens lifted directly from the Modernist design system
// (design_handoff_shelfinity_redesign/design/_ds/modernist-.../styles.css).
// Single source of truth for theme.js and any component that needs a raw
// token value outside the MUI theme (e.g. an inline SVG stroke color).

export const colors = {
  bg: '#f3f2f2',
  surface: '#eae9e9',
  text: '#201e1d',
  accent: '#ec3013',
  accent2: '#e15b47',
  divider: 'rgba(32, 30, 29, 0.4)',
  textMuted: 'rgba(32, 30, 29, 0.55)',
  textFaint: 'rgba(32, 30, 29, 0.7)',
  neutral: {
    100: '#f8f4f4',
    200: '#eae7e7',
    300: '#d7d3d3',
    400: '#bab6b6',
    500: '#9b9797',
    600: '#7d7979',
    700: '#605d5d',
    800: '#444141',
    900: '#2d2b2b',
  },
  accentRamp: {
    100: '#fff2ef',
    200: '#ffe0d9',
    300: '#ffc4b8',
    400: '#ff9783',
    500: '#ff563c',
    600: '#dd2b0f',
    700: '#ae1800',
    800: '#7c1405',
    900: '#4d170e',
  },
};

// MUI's spacing base unit is set to this in theme.js, so theme.spacing(n)
// reproduces --space-n from the token file exactly (space-5/7 don't exist
// in the design and are simply unused, not skipped over).
export const SPACING_UNIT_PX = 4;

export const fontFamily = '"Archivo", system-ui, sans-serif';

export const shadows = {
  sm: '0 1px 2px rgba(45, 43, 43, 0.14)',
  md: '0 3px 10px rgba(45, 43, 43, 0.16)',
  lg: '0 12px 32px rgba(45, 43, 43, 0.22)',
};
