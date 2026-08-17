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
import List from '@mui/material/List';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Divider from '@mui/material/Divider';
import Box from '@mui/material/Box';
import Avatar from '@mui/material/Avatar';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import IconButton from '@mui/material/IconButton';
import Chip from '@mui/material/Chip';
import Alert from '@mui/material/Alert';
import DashboardIcon from '@mui/icons-material/DashboardOutlined';
import MenuBookIcon from '@mui/icons-material/MenuBookOutlined';
import AssignmentIcon from '@mui/icons-material/AssignmentOutlined';
import FactCheckIcon from '@mui/icons-material/FactCheckOutlined';
import PeopleIcon from '@mui/icons-material/PeopleOutlined';
import LibraryBooksIcon from '@mui/icons-material/LibraryBooksOutlined';
import BookmarkIcon from '@mui/icons-material/BookmarkBorderOutlined';
import WatchLaterIcon from '@mui/icons-material/WatchLaterOutlined';
import MailIcon from '@mui/icons-material/MailOutlineOutlined';
import BarChartIcon from '@mui/icons-material/BarChartOutlined';
import LogoutIcon from '@mui/icons-material/LogoutOutlined';
import AutoStoriesIcon from '@mui/icons-material/AutoStoriesOutlined';
import { useAuth } from '../context/AuthContext';

const DRAWER_WIDTH = 240;

const userNavItems = [
  { to: '/', label: 'Dashboard', icon: <DashboardIcon /> },
  { to: '/books', label: 'Browse Books', icon: <MenuBookIcon /> },
  { to: '/my-activity', label: 'My Activity', icon: <AssignmentIcon /> },
];

const adminNavItems = [
  { to: '/admin/requests', label: 'Requests', icon: <FactCheckIcon /> },
  { to: '/admin/users', label: 'Users', icon: <PeopleIcon /> },
  { to: '/admin/books', label: 'Manage Books', icon: <LibraryBooksIcon /> },
  { to: '/admin/reservations', label: 'Reservations', icon: <BookmarkIcon /> },
  { to: '/admin/overdue', label: 'Overdue', icon: <WatchLaterIcon /> },
  { to: '/admin/email-config', label: 'Email Config', icon: <MailIcon /> },
  { to: '/admin/reports', label: 'Reports', icon: <BarChartIcon /> },
];

function NavList({ items, onNavigate }) {
  return (
    <List sx={{ px: 1 }}>
      {items.map((item) => (
        <ListItemButton
          key={item.to}
          component={NavLink}
          to={item.to}
          end={item.to === '/'}
          onClick={onNavigate}
          sx={{
            borderRadius: 2,
            mb: 0.5,
            '&.active': {
              bgcolor: 'primary.main',
              color: 'primary.contrastText',
              '& .MuiListItemIcon-root': { color: 'primary.contrastText' },
              '&:hover': { bgcolor: 'primary.dark' },
            },
          }}
        >
          <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
          <ListItemText primary={item.label} primaryTypographyProps={{ fontSize: 14, fontWeight: 600 }} />
        </ListItemButton>
      ))}
    </List>
  );
}

export default function Layout() {
  const { currentUser, isAdmin, isPendingApproval, logout } = useAuth();
  const navigate = useNavigate();
  const [menuAnchor, setMenuAnchor] = useState(null);
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = () => {
    setMenuAnchor(null);
    logout();
    navigate('/login');
  };

  const drawerContent = (
    <Box display="flex" flexDirection="column" height="100%">
      <Toolbar>
        <AutoStoriesIcon sx={{ mr: 1, color: 'primary.main' }} />
        <Typography variant="h6" fontWeight={700}>Shelfinity</Typography>
      </Toolbar>
      <Divider />
      <NavList items={userNavItems} onNavigate={() => setMobileOpen(false)} />
      {isAdmin && (
        <>
          <Divider sx={{ mx: 2 }} />
          <Typography variant="overline" sx={{ px: 3, pt: 1, color: 'text.secondary' }}>
            Admin
          </Typography>
          <NavList items={adminNavItems} onNavigate={() => setMobileOpen(false)} />
        </>
      )}
    </Box>
  );

  return (
    <Box display="flex" minHeight="100vh">
      <AppBar
        position="fixed"
        color="inherit"
        sx={{ bgcolor: 'background.paper', width: { md: `calc(100% - ${DRAWER_WIDTH}px)` }, ml: { md: `${DRAWER_WIDTH}px` } }}
      >
        <Toolbar sx={{ justifyContent: 'flex-end', gap: 2 }}>
          {isAdmin && <Chip label="Admin" color="primary" size="small" />}
          <IconButton onClick={(e) => setMenuAnchor(e.currentTarget)} data-testid="user-menu-button">
            <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.main' }}>
              {(currentUser?.name || '?').charAt(0).toUpperCase()}
            </Avatar>
          </IconButton>
          <Menu anchorEl={menuAnchor} open={Boolean(menuAnchor)} onClose={() => setMenuAnchor(null)}>
            <Box sx={{ px: 2, py: 1 }}>
              <Typography variant="subtitle2" fontWeight={700}>{currentUser?.name}</Typography>
              <Typography variant="caption" color="text.secondary">{currentUser?.email}</Typography>
            </Box>
            <Divider />
            <MenuItem onClick={handleLogout}>
              <ListItemIcon><LogoutIcon fontSize="small" /></ListItemIcon>
              Sign out
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { md: DRAWER_WIDTH }, flexShrink: { md: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ display: { xs: 'block', md: 'none' }, '& .MuiDrawer-paper': { width: DRAWER_WIDTH } }}
        >
          {drawerContent}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{ display: { xs: 'none', md: 'block' }, '& .MuiDrawer-paper': { width: DRAWER_WIDTH, borderRight: '1px solid rgba(0,0,0,0.08)' } }}
          open
        >
          {drawerContent}
        </Drawer>
      </Box>

      <Box component="main" flexGrow={1} sx={{ width: { md: `calc(100% - ${DRAWER_WIDTH}px)` } }}>
        <Toolbar />
        <Box sx={{ p: { xs: 2, md: 4 } }}>
          {isPendingApproval && (
            <Alert severity="info" sx={{ mb: 3 }}>
              Your account is pending admin approval. You'll be able to borrow, return, and reserve books once
              approved.
            </Alert>
          )}
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
