/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */

// Single mapping from the app's existing status enums to a label + tag
// variant, so no page invents its own wording or color for the same state.
// Meaning is carried by the word; variant only ranks it (see StatusTag).
//
// Existing enums this maps (backend is unchanged — see UserRole.java,
// QueueStatus.java, ReservationStatus.java): QueueStatus PENDING/APPROVED/
// REJECTED, ReservationStatus ACTIVE/NOTIFIED/FULFILLED/CANCELLED/EXPIRED,
// User.active, User.role USER/ADMIN.

export function queueStatusInfo(status) {
  switch (status) {
    case 'PENDING':
      return { label: 'Waiting on the library', variant: 'accent' };
    case 'APPROVED':
      return { label: 'Approved', variant: 'neutral' };
    case 'REJECTED':
      return { label: 'Declined', variant: 'outline' };
    default:
      return { label: status || 'Unknown', variant: 'neutral' };
  }
}

export function reservationStatusInfo(status) {
  switch (status) {
    case 'ACTIVE':
      return { label: 'Waiting on the library', variant: 'accent' };
    case 'NOTIFIED':
      return { label: 'Ready for pickup', variant: 'accent' };
    case 'FULFILLED':
      return { label: 'Fulfilled', variant: 'neutral' };
    case 'CANCELLED':
      return { label: 'Cancelled', variant: 'neutral' };
    case 'EXPIRED':
      return { label: 'Expired', variant: 'neutral' };
    default:
      return { label: status || 'Unknown', variant: 'neutral' };
  }
}

export function userStatusInfo(active) {
  return active
    ? { label: 'Active', variant: 'neutral' }
    : { label: 'Pending approval', variant: 'accent' };
}

export function roleInfo(role) {
  // Roles are always outline, per the design's status vocabulary — they
  // rank differently from a state, they're a fact about the person.
  return { label: role === 'ADMIN' ? 'Admin' : 'Member', variant: 'outline' };
}

const MS_PER_DAY = 24 * 60 * 60 * 1000;
const DAY_NAMES = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

/**
 * Status for an approved borrow with a due date — "Due in N days" / "Due
 * Friday" (inside 3 days — named per the design's copy rule) / "N days
 * overdue" (the one solid-accent tag in the product).
 */
export function loanStatusInfo(dueDate) {
  if (!dueDate) return { label: 'Due date not set', variant: 'neutral' };
  const due = new Date(dueDate);
  const now = new Date();
  const daysLeft = Math.ceil((due.setHours(0, 0, 0, 0) - now.setHours(0, 0, 0, 0)) / MS_PER_DAY);

  if (daysLeft < 0) {
    const overdueDays = Math.abs(daysLeft);
    return {
      label: `${overdueDays} ${overdueDays === 1 ? 'day' : 'days'} overdue`,
      variant: 'solid',
    };
  }
  if (daysLeft === 0) {
    return { label: 'Due today', variant: 'accent' };
  }
  if (daysLeft <= 3) {
    return { label: `Due ${DAY_NAMES[due.getDay()]}`, variant: 'accent' };
  }
  return { label: `Due in ${daysLeft} days`, variant: 'neutral' };
}
