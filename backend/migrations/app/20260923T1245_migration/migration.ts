#!/usr/bin/env -S node
import type { Contract as End } from '../../snapshots/3a0d3c87dd61a31860c6455e695a2505269cacf7152ff8d2478c2877b8052306/contract';
import endContract from '../../snapshots/3a0d3c87dd61a31860c6455e695a2505269cacf7152ff8d2478c2877b8052306/contract.json' with { type: 'json' };
import type { Contract as Start } from '../../snapshots/6beb241432242911dadceb8cb600e50ab8bad2d0cba2668bd2a0f23f12f854f0/contract';
import startContract from '../../snapshots/6beb241432242911dadceb8cb600e50ab8bad2d0cba2668bd2a0f23f12f854f0/contract.json' with { type: 'json' };
import { Migration, MigrationCLI, col } from '@prisma/orm-postgres/migration';

export default class M extends Migration<Start, End> {
  override readonly startContractJson = startContract;
  override readonly endContractJson = endContract;

  override get operations() {
    return [
      this.addColumn({
        schema: 'public',
        table: 'transaction',
        column: col('transferGroupId', 'text', { codecRef: { codecId: 'pg/text@1' } }),
      }),
    ];
  }
}

MigrationCLI.run(import.meta.url, M);
