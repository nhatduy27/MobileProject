# Voucher Usage ShopId Backfill Migration

**Status:** Ready for manual execution  
**Safety:** Idempotent, dry-run mode available  
**Target:** One-time migration for VOUCH-009 Phase 3  

---

## Overview

This migration script populates the `shopId` field for legacy `voucherUsages` documents that are missing it.

### Why This Migration?

- **Phase 2** of VOUCH-009 added `shopId` denormalization to voucherUsages for efficient DB-level filtering
- **Legacy records** created before Phase 2 don't have `shopId` populated
- **Phase 3** implemented hybrid filtering that derives `shopId` from vouchers (for backward compatibility)
- **This script** migrates legacy records to the new format, enabling fast-path filtering for all records

### Expected Impact

After migration:
- All voucherUsages records will have `shopId` field
- Fast-path filtering becomes available for all records (no enrichment needed)
- Hybrid filtering still works, but path becomes 100% fast-path
- Query performance improves as DB-level filtering becomes primary strategy

---

## How It Works

### Algorithm

```
1. Query for legacy docs: WHERE shopId == null OR shopId MISSING
2. For each batch (500 docs by default):
   a. Collect all voucherIds from batch
   b. Batch fetch all vouchers (chunked in groups of 10 for 'in' operator)
   c. For each usage doc:
      - Get corresponding voucher
      - Set shopId = voucher.shopId (or null if voucher not found)
   d. Batch write all updates
3. Repeat until all legacy docs processed
4. Log summary: total processed, updated, errors
```

### Idempotency & Safety

✅ **Idempotent:** Skips docs that already have `shopId` (only targets null/missing)  
✅ **Graceful Failure:** If voucher not found, sets `shopId = null` and logs it  
✅ **Batch Limits:** Respects Firestore's 500-write and 10-item-in-query limits  
✅ **Error Recovery:** Continues processing if individual record fails  
✅ **Dry-Run Mode:** Preview all changes without committing  

### Performance

- **Batch Size:** 500 writes per commit (configurable)
- **Chunking:** Voucher lookup chunked at 10 items (Firestore limit)
- **Progress:** Logged every 100 records
- **Time:** ~1-5 minutes for typical dataset (depends on size and quota)

---

## Usage

### Prerequisites

- You must be in the `functions/` directory
- `service-account.json` must be present in the functions directory
  - Download from Firebase Console: Project Settings → Service Accounts → Generate New Key
  - Place in: `MobileProject/Backend/functions/service-account.json`
  - **⚠️ NEVER commit this file to git** (add to .gitignore)
- Node.js and ts-node installed
- Proper permissions on Firestore
- TypeScript compilation working: `npm run build` should succeed

### 1. Dry Run (Recommended First Step)

**Preview changes without committing:**

```bash
cd MobileProject/Backend/functions

npx ts-node scripts/backfill-voucher-usage-shopid.ts --dry-run
```

**Expected output:**
```
📊 Backfill ShopId for Legacy Voucher Usage Records
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Mode: DRY RUN (no changes)
Batch size: 500

🔍 Scanning for legacy records (shopId missing/null)...
Found 1234 legacy records

⚙️  Processing 1234 records in batches of 500...

  ✓ Batch complete: 500/1234
  ✓ Batch complete: 1000/1234
  ✓ Final batch complete: 1234/1234

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📈 Migration Summary
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Total Processed:  1234
Total Updated:    1234
Total Skipped:    0
Total Errors:     2

⚠️  Error Details:
  • doc_id_123: Voucher not found: voucher_abc
  • doc_id_456: Voucher not found: voucher_xyz

✅ Dry run complete! No changes were made to database.
   Run without --dry-run flag to commit changes.
```

### 2. Actual Migration

**After verifying the dry run, execute the migration:**

```bash
npx ts-node scripts/backfill-voucher-usage-shopid.ts
```

**Expected output:** Same as dry-run, but with:
```
Mode: MIGRATION (will commit)
...
✅ Migration complete! All records have been updated.
```

### 3. Custom Batch Size (Optional)

```bash
# Use batch size of 100 instead of default 500
npx ts-node scripts/backfill-voucher-usage-shopid.ts --batch-size 100
```

---

## Monitoring & Verification

### After Migration

1. **Check Summary Stats**
   - All legacy records should be updated
   - Errors should be minimal (only if voucher missing)

2. **Verify in Firestore Console**
   - Collection: `voucherUsages`
   - Query: `shopId != null` (should now include all previously-null records)

3. **Run Tests**
   ```bash
   npm test -- --testPathPattern="vouchers.service.spec"
   ```

4. **Monitor Filtering Performance**
   - B.3 test (shopId filter) should now use 100% fast path
   - No enrichment needed for any record

---

## Rollback (If Needed)

If migration causes issues:

1. **Delete `shopId` field** from newly-migrated records:
   ```typescript
   // Pseudo-code to run in Firestore console or script
   db.collection('voucherUsages')
     .where('createdAt', '>=', migrationStartTime)
     .delete() // or update with FieldValue.delete()
   ```

2. **Revert code changes** to Phase 3 hybrid filtering

3. **Restore from backup** if available

---

## Troubleshooting

### "Found 0 legacy records"
- ✅ Migration already complete!
- Or: No voucherUsages with `shopId = null`

### "Total Errors > 0"
- This is expected and **safe** - vouchers may have been deleted
- Script sets `shopId = null` for missing vouchers (graceful fallback)
- Hybrid filtering still works (falls back to enrichment for null shopId)

### "Permission denied" error
- Check Firebase Admin SDK credentials (service-account.json)
- Verify `service-account.json` is in functions/ directory
- Download fresh service-account.json from Firebase Console
- Ensure your account has Firestore write permissions

### "Unable to compile TypeScript" error
- Make sure ts-node is installed: `npm install`
- Verify Node.js version compatibility
- Try clearing cache: `rm -rf node_modules && npm install`

---

## Technical Details

### Query Pattern

```typescript
// Finds all legacy docs
db.collection('voucherUsages')
  .where('shopId', '==', null)
  .get()
```

**Note:** This only finds docs where field is explicitly null. Documents created before the field existed may need additional handling. Current implementation targets explicit nulls, which is safe and sufficient.

### Batch Operations

```typescript
// Respectful of Firestore limits
const batch = db.batch();

// Max 500 writes per batch
for (let i = 0; i < updates.length; i += 500) {
  const chunk = updates.slice(i, i + 500);
  // ... batch.update() each
  batch.commit();
}

// Chunked voucher lookup (max 10 items per 'in' operator)
for (let i = 0; i < voucherIds.length; i += 10) {
  const chunk = voucherIds.slice(i, i + 10);
  db.collection('vouchers')
    .where(FieldPath.documentId(), 'in', chunk)
    .get();
}
```

### Error Handling

- **Voucher not found:** Sets `shopId = null`, logs error, continues
- **Update fails:** Logs error, continues to next batch
- **Network error:** Throws and exits (batch partially committed, can be retried)

---

## Related Documentation

- [VOUCH-009-PHASE3-FIX-SUMMARY.md](../../VOUCH-009-PHASE3-FIX-SUMMARY.md) - Phase 3 implementation details
- [firestore-vouchers.repository.ts](../src/modules/vouchers/repositories/firestore-vouchers.repository.ts) - Hybrid filtering code
- [VOUCHER_SWAGGER_TEST_CHECKLIST.md](../../docs/backend/tests/VOUCHER_SWAGGER_TEST_CHECKLIST.md) - Test B.3 documentation

---

## Schedule & Planning

- **When:** Schedule during low-traffic period (e.g., 2-4 AM)
- **Duration:** ~1-5 minutes depending on dataset size
- **Frequency:** One-time only (idempotent, safe to re-run)
- **Coordination:** Notify team before running in production

---

## Success Criteria

✅ Script completes without fatal errors  
✅ Summary shows "Total Errors" is minimal or 0  
✅ All voucherUsages now have `shopId` (null or valid ID)  
✅ Tests pass: `npm test`  
✅ B.3 test uses fast-path filtering exclusively  

---

## Contact & Support

For questions or issues:
1. Check this README first
2. Review VOUCH-009-PHASE3-FIX-SUMMARY.md
3. Check error logs in script output
4. Contact backend team
