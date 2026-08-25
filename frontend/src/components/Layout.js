/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Drawer from '@mui/material/Drawer';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import Alert from '@mui/material/Alert';
import {
  BookOpen,
  LayoutDashboard,
  BookMarked,
  ClipboardList,
  ListChecks,
  Users,
  Library,
  Bookmark,
  Clock,
  Mail,
  BarChart3,
  Menu as MenuIcon,
  ExternalLink,
} from 'lucide-react';
import { colors } from '../theme/tokens';
import { DOCS_URL } from './FooterCredit';
import { useAuth } from '../context/AuthContext';

const RAIL_WIDTH = 240;

const myLibraryItems = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/books', label: 'Browse Books', icon: BookMarked },
  { to: '/my-activity', label: 'My Shelf', icon: ClipboardList },
];

const manageLibraryItems = [
  { to: '/admin/requests', label: 'Requests', icon: ListChecks },
  { to: '/admin/users', label: 'Users', icon: Users },
  { to: '/admin/books', label: 'Manage Books', icon: Library },
  { to: '/admin/reservations', label: 'Reservations', icon: Bookmark },
  { to: '/admin/overdue', label: 'Overdue', icon: Clock },
  { to: '/admin/email-config', label: 'Email Config', icon: Mail },
  { to: '/admin/reports', label: 'Reports', icon: BarChart3 },
];

function NavGroup({ label, items, onNavigate, withTopRule }) {
  return (
    <Box sx={{ px: 2, pt: withTopRule ? 2 : 2.5 }}>
      {withTopRule && <Box sx={{ height: 2, bgcolor: colors.divider, mb: 2 }} />}
      <Typography
        sx={{
          fontSize: 11,
          fontWeight: 600,
          letterSpacing: '0.08em',
          textTransform: 'uppercase',
          color: colors.textMuted,
          px: 1,
          mb: 1,
        }}
      >
        {label}
      </Typography>
      <Box component="nav" sx={{ display: 'flex', flexDirection: 'column' }}>
        {items.map(({ to, label: itemLabel, icon: Icon, end }) => (
          <Box
            key={to}
            component={NavLink}
            to={to}
            end={end}
            onClick={onNavigate}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1.25,
              height: 40,
              px: 1,
              fontSize: 14,
              color: colors.text,
              textDecoration: 'none',
              borderLeft: '3px solid transparent',
              '&:hover': { backgroundColor: 'rgba(32, 30, 29, 0.06)' },
              '&.active': {
                borderLeftColor: colors.accent,
                color: colors.accent,
                fontWeight: 600,
              },
            }}
          >
            <Icon size={16} strokeWidth={2} style={{ flexShrink: 0 }} />
            <Typography sx={{ fontSize: 14, fontWeight: 'inherit', color: 'inherit' }}>
              {itemLabel}
            </Typography>
          </Box>
        ))}
      </Box>
    </Box>
  );
}

function RailFoot({ currentUser, isAdmin, onSignOut }) {
  const initial = (currentUser?.name || '?').charAt(0).toUpperCase();
  return (
    <Box sx={{ mt: 'auto', borderTop: `2px solid ${colors.divider}`, p: 2 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, mb: 1.5 }}>
        <Box
          sx={{
            width: 34,
            height: 34,
            flexShrink: 0,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            bgcolor: colors.text,
            color: colors.bg,
            fontWeight: 800,
            fontSize: 14,
          }}
        >
          {initial}
        </Box>
        <Box sx={{ minWidth: 0 }}>
          <Typography sx={{ fontSize: 13, fontWeight: 600, lineHeight: 1.3 }} noWrap>
            {currentUser?.name || 'Account'}
          </Typography>
          <Typography sx={{ fontSize: 11, color: colors.textMuted, lineHeight: 1.3 }}>
            {isAdmin ? 'Admin' : 'Member'}
          </Typography>
        </Box>
      </Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <Box
          component="button"
          onClick={onSignOut}
          data-testid="user-menu-button"
          sx={{
            border: 'none',
            background: 'none',
            padding: 0,
            font: 'inherit',
            fontSize: 13,
            fontWeight: 600,
            color: colors.accent,
            cursor: 'pointer',
            '&:hover': { textDecoration: 'underline' },
          }}
        >
          Sign out
        </Box>
        <Box
          component="a"
          href={DOCS_URL}
          target="_blank"
          rel="noopener noreferrer"
          sx={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: 0.5,
            fontSize: 13,
            fontWeight: 600,
            color: colors.textMuted,
            textDecoration: 'none',
            '&:hover': { color: colors.text, textDecoration: 'underline' },
          }}
        >
          Docs
          <ExternalLink size={12} strokeWidth={2} />
        </Box>
      </Box>
    </Box>
  );
}

export default function Layout() {
  const { currentUser, isAdmin, isPendingApproval, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = () => {
    setMobileOpen(false);
    logout();
    navigate('/login');
  };

  const railContent = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          height: 60,
          px: 2,
          borderBottom: `2px solid ${colors.divider}`,
        }}
      >
        <BookOpen size={20} strokeWidth={2.25} color={colors.accent} />
        <Typography sx={{ fontSize: 18, fontWeight: 800 }}>Shelfinity</Typography>
      </Box>

      <NavGroup label="My Library" items={myLibraryItems} onNavigate={() => setMobileOpen(false)} />
      {isAdmin && (
        <NavGroup
          label="Manage Library"
          items={manageLibraryItems}
          onNavigate={() => setMobileOpen(false)}
          withTopRule
        />
      )}

      <RailFoot currentUser={currentUser} isAdmin={isAdmin} onSignOut={handleLogout} />
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <AppBar
        position="fixed"
        color="inherit"
        sx={{
          display: { md: 'none' },
          bgcolor: colors.bg,
        }}
      >
        <Toolbar sx={{ gap: 1 }}>
          <IconButton
            edge="start"
            onClick={() => setMobileOpen(true)}
            aria-label="Open navigation"
          >
            <MenuIcon size={20} />
          </IconButton>
          <BookOpen size={18} strokeWidth={2.25} color={colors.accent} />
          <Typography sx={{ fontSize: 16, fontWeight: 800 }}>Shelfinity</Typography>
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { md: RAIL_WIDTH }, flexShrink: { md: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ display: { xs: 'block', md: 'none' }, '& .MuiDrawer-paper': { width: RAIL_WIDTH } }}
        >
          {railContent}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', md: 'block' },
            '& .MuiDrawer-paper': { width: RAIL_WIDTH, borderRight: `2px solid ${colors.divider}` },
          }}
          open
        >
          {railContent}
        </Drawer>
      </Box>

      <Box component="main" sx={{ flexGrow: 1, width: { md: `calc(100% - ${RAIL_WIDTH}px)` }, bgcolor: colors.bg }}>
        <Toolbar sx={{ display: { md: 'none' } }} />
        <Box sx={{ px: { xs: 2.5, md: 5 }, py: { xs: 3, md: 4 } }}>
          {isPendingApproval && (
            <Alert severity="info" sx={{ mb: 3 }}>
              Your account is pending admin approval. You&apos;ll be able to borrow, return, and
              reserve books once approved.
            </Alert>
          )}
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
