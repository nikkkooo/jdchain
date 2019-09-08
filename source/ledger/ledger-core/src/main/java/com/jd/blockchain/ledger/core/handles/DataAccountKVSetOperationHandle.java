package com.jd.blockchain.ledger.core.handles;

import com.jd.blockchain.ledger.DataAccountDoesNotExistException;
import com.jd.blockchain.ledger.DataAccountKVSetOperation;
import com.jd.blockchain.ledger.DataAccountKVSetOperation.KVWriteEntry;
import com.jd.blockchain.ledger.DataVersionConflictException;
import com.jd.blockchain.ledger.LedgerPermission;
import com.jd.blockchain.ledger.core.DataAccount;
import com.jd.blockchain.ledger.core.LedgerDataQuery;
import com.jd.blockchain.ledger.core.LedgerDataset;
import com.jd.blockchain.ledger.core.LedgerService;
import com.jd.blockchain.ledger.core.MultiIDsPolicy;
import com.jd.blockchain.ledger.core.OperationHandleContext;
import com.jd.blockchain.ledger.core.SecurityContext;
import com.jd.blockchain.ledger.core.SecurityPolicy;
import com.jd.blockchain.ledger.core.TransactionRequestExtension;
import com.jd.blockchain.utils.Bytes;

public class DataAccountKVSetOperationHandle extends AbstractLedgerOperationHandle<DataAccountKVSetOperation> {
	public DataAccountKVSetOperationHandle() {
		super(DataAccountKVSetOperation.class);
	}

	@Override
	protected void doProcess(DataAccountKVSetOperation kvWriteOp, LedgerDataset newBlockDataset,
			TransactionRequestExtension requestContext, LedgerDataQuery previousBlockDataset,
			OperationHandleContext handleContext, LedgerService ledgerService) {
		// 权限校验；
		SecurityPolicy securityPolicy = SecurityContext.getContextUsersPolicy();
		securityPolicy.checkEndpointPermission(LedgerPermission.WRITE_DATA_ACCOUNT, MultiIDsPolicy.AT_LEAST_ONE);

		// 操作账本；
		DataAccount account = newBlockDataset.getDataAccountSet().getDataAccount(kvWriteOp.getAccountAddress());
		if (account == null) {
			throw new DataAccountDoesNotExistException("DataAccount doesn't exist!");
		}
		KVWriteEntry[] writeSet = kvWriteOp.getWriteSet();
		long v = -1;
		for (KVWriteEntry kvw : writeSet) {
			v = account.setBytes(Bytes.fromString(kvw.getKey()), kvw.getValue(), kvw.getExpectedVersion());
			if (v < 0) {
				throw new DataVersionConflictException();
			}
		}
	}

}
