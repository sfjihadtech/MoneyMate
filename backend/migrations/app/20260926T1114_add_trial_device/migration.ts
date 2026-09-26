#!/usr/bin/env -S node
import type { Contract as Start } from '../../snapshots/3a0d3c87dd61a31860c6455e695a2505269cacf7152ff8d2478c2877b8052306/contract';
import startContract from '../../snapshots/3a0d3c87dd61a31860c6455e695a2505269cacf7152ff8d2478c2877b8052306/contract.json' with { type: 'json' };
import type { Contract as End } from '../../snapshots/eaacbdbbec455c0a05154f3e3a708b363f05a40999238bc9b7a5b72108e39dad/contract';
import endContract from '../../snapshots/eaacbdbbec455c0a05154f3e3a708b363f05a40999238bc9b7a5b72108e39dad/contract.json' with { type: 'json' };
import { Migration, MigrationCLI, col, fn, primaryKey } from '@prisma/orm-postgres/migration';

export default class M extends Migration<Start, End> {
  override readonly startContractJson = startContract;
  override readonly endContractJson = endContract;

  override get operations() {
    return [
      this.createTable({
        schema: 'public',
        table: 'trialDevice',
        columns: [
          col('createdAt', 'timestamptz', {
            notNull: true,
            default: fn('now()'),
            codecRef: { codecId: 'pg/timestamptz-string@1' },
          }),
          col('deviceHash', 'text', { notNull: true, codecRef: { codecId: 'pg/text@1' } }),
          col('id', 'SERIAL', { notNull: true, codecRef: { codecId: 'pg/int4@1' } }),
          col('trialStartedAt', 'timestamptz', {
            notNull: true,
            default: fn('now()'),
            codecRef: { codecId: 'pg/timestamptz-string@1' },
          }),
          col('userId', 'int4', { notNull: true, codecRef: { codecId: 'pg/int4@1' } }),
        ],
        constraints: [primaryKey(['id'])],
      }),
      this.addUnique({
        schema: 'public',
        table: 'trialDevice',
        constraint: 'trialDevice_userId_key',
        columns: ['userId'],
      }),
      this.addUnique({
        schema: 'public',
        table: 'trialDevice',
        constraint: 'trialDevice_deviceHash_key',
        columns: ['deviceHash'],
      }),
      this.addForeignKey({
        schema: 'public',
        table: 'trialDevice',
        foreignKey: {
          name: 'trialDevice_userId_fkey',
          columns: ['userId'],
          references: { schema: 'public', table: 'user', columns: ['id'] },
        },
      }),
    ];
  }
}

MigrationCLI.run(import.meta.url, M);
