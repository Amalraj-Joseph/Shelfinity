/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
// MUI X DataGrid virtualizes rows based on its container's measured size,
// which jsdom always reports as zero — without these, no row DOM ever
// renders and a test can't distinguish "shows a UUID" from "shows nothing".
export default function mockDataGridLayout() {
  global.ResizeObserver = class {
    observe() {}
    unobserve() {}
    disconnect() {}
  };
  Object.defineProperty(HTMLElement.prototype, 'offsetHeight', { configurable: true, value: 600 });
  Object.defineProperty(HTMLElement.prototype, 'offsetWidth', { configurable: true, value: 1000 });
  Object.defineProperty(HTMLElement.prototype, 'getBoundingClientRect', {
    configurable: true,
    value: () => ({ width: 1000, height: 600, top: 0, left: 0, bottom: 0, right: 0 }),
  });
}
