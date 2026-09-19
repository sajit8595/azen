// Helper to unwrap the uniform ApiResponse envelope.
// On success returns `data`; on an error envelope throws with the first message.

import { ApiResponse } from './models';

export function unwrap<T>(res: ApiResponse<T>): T {
  if (res.errors && res.errors.length > 0) {
    throw new Error(res.errors[0].message);
  }
  return res.data as T;
}
