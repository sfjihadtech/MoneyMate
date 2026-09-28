#!/usr/bin/env -S node
import type { Contract as End } from '../../snapshots/4bacf75ad0ce30e388d1ff2262ccf97310877d7db2b20fcea8c404af7d441118/contract';
import endContract from '../../snapshots/4bacf75ad0ce30e388d1ff2262ccf97310877d7db2b20fcea8c404af7d441118/contract.json' with { type: 'json' };
import type { Contract as Start } from '../../snapshots/eaacbdbbec455c0a05154f3e3a708b363f05a40999238bc9b7a5b72108e39dad/contract';
import startContract from '../../snapshots/eaacbdbbec455c0a05154f3e3a708b363f05a40999238bc9b7a5b72108e39dad/contract.json' with { type: 'json' };
import { Migration, MigrationCLI, col, fn, primaryKey } from '@prisma/orm-postgres/migration';

export default class M extends Migration<Start, End> {
  override readonly startContractJson = startContract;
  override readonly endContractJson = endContract;

  override get operations() {
    return [
      this.createTable({
        schema: 'public',
        table: 'fcmDevice',
        columns: [
          col('createdAt', 'timestamptz', {
            notNull: true,
            default: fn('now()'),
            codecRef: { codecId: 'pg/timestamptz-string@1' },
          }),
          col('id', 'SERIAL', { notNull: true, codecRef: { codecId: 'pg/int4@1' } }),
          col('token', 'text', { notNull: true, codecRef: { codecId: 'pg/text@1' } }),
          col('updatedAt', 'timestamptz', {
            notNull: true,
            codecRef: { codecId: 'pg/timestamptz-string@1' },
          }),
          col('userId', 'int4', { notNull: true, codecRef: { codecId: 'pg/int4@1' } }),
        ],
        constraints: [primaryKey(['id'])],
      }),
      this.addUnique({
        schema: 'public',
        table: 'fcmDevice',
        constraint: 'fcmDevice_token_key',
        columns: ['token'],
      }),
      this.createIndex({
        schema: 'public',
        table: 'fcmDevice',
        index: 'fcmDevice_userId_idx_a489d58a',
        columns: ['userId'],
      }),
      this.addForeignKey({
        schema: 'public',
        table: 'fcmDevice',
        foreignKey: {
          name: 'fcmDevice_userId_fkey',
          columns: ['userId'],
          references: { schema: 'public', table: 'user', columns: ['id'] },
        },
      }),
    ];
  }
}

MigrationCLI.run(import.meta.url, M);
