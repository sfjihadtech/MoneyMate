// =============================================================================
// File: db.ts
// Purpose: Prisma/database helper source for db.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

import 'dotenv/config';
import postgres from '@prisma/orm-postgres/runtime';
import type { Contract } from './contract.d';
import contractJson from './contract.json' with { type: 'json' };

export const db = postgres<Contract>({
  contractJson,
  url: process.env['DATABASE_URL']!,
});
