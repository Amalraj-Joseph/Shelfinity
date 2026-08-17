/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
// react-scripts test auto-loads this file. @testing-library/jest-dom has
// been a dependency since before this rewrite, but nothing ever imported it
// here, so its custom matchers (toHaveTextContent, toBeInTheDocument, etc.)
// were never actually registered — there were no tests to reveal the gap
// until now.
import '@testing-library/jest-dom';
