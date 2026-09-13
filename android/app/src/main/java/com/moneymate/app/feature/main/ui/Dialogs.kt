package com.moneymate.app.feature.main.ui

// =============================================================================
// File: Dialogs.kt
// Purpose: Bottom sheets and dialogs for transactions, accounts, budgets, goals, bills, transfers, and related actions.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneymate.app.core.common.CategoryCatalog
import com.moneymate.app.data.model.*
import com.moneymate.app.feature.main.MoneyMateState
import com.moneymate.app.ui.theme.LocalMoneyMateTokens
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset


// -----------------------------------------------------------------------------
// Section: dateToIso
// Purpose: Formatting/helper logic for date To Iso.
// -----------------------------------------------------------------------------
private fun dateToIso(date: String): String? = runCatching {
    LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC).toString()
}.getOrNull()


// -----------------------------------------------------------------------------
// Section: HtmlSheet
// Purpose: Renders and manages the Html Sheet modal UI flow.
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HtmlSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = LocalMoneyMateTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.surface,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = c.border) }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 22.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = c.primaryText, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = c.secondaryText)
                }
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun <T> PickerField(
    label: String,
    items: List<T>,
    selected: T?,
    itemLabel: (T) -> String,
    onSelect: (T) -> Unit
) {
    val c = LocalMoneyMateTokens.current
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(if (selected == null) label else itemLabel(selected), color = c.primaryText)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(text = { Text(itemLabel(item)) }, onClick = { onSelect(item); expanded = false })
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: SheetField
// Purpose: Renders and manages the Sheet Field modal UI flow.
// -----------------------------------------------------------------------------
@Composable
private fun SheetField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = minLines == 1,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp)
    )
}


// -----------------------------------------------------------------------------
// Section: AddTransactionDialog
// Purpose: Renders and manages the Add Transaction Dialog modal UI flow.
// -----------------------------------------------------------------------------
@Composable
fun AddTransactionDialog(
    state: MoneyMateState,
    initial: Transaction? = null,
    onDismiss: () -> Unit
) {
    val c = LocalMoneyMateTokens.current
    val scope = rememberCoroutineScope()
    var type by remember { mutableStateOf(initial?.type ?: "expense") }
    var amount by remember { mutableStateOf(initial?.amount ?: "") }
    var merchant by remember { mutableStateOf(initial?.merchant ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var payment by remember { mutableStateOf(initial?.paymentMethod ?: "Card") }
    var date by remember { mutableStateOf(initial?.occurredAt?.take(10) ?: LocalDate.now().toString()) }
    var time by remember { mutableStateOf("12:00") }
    var receiptAttached by remember { mutableStateOf(false) }
    var account by remember { mutableStateOf(state.accounts.firstOrNull { it.id == initial?.accountId } ?: state.accounts.firstOrNull()) }
    var category by remember { mutableStateOf(state.categories.firstOrNull { it.id == initial?.categoryId }) }
    var saving by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    val relevantCategories = CategoryCatalog.ordered(state.categories, type)

    HtmlSheet(if (initial == null) mmText(state,"New Transaction","নতুন লেনদেন") else mmText(state,"Edit Transaction","লেনদেন সম্পাদনা"), onDismiss) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            Surface(modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(14.dp), color = c.background) {
                Row(Modifier.padding(4.dp)) {
                    listOf("expense" to mmText(state,"Expense","খরচ"), "income" to mmText(state,"Income","আয়")).forEach { (v,label) ->
                        val sel = type == v
                        Surface(onClick={ type=v; category=null }, modifier=Modifier.weight(1f).height(42.dp), shape=RoundedCornerShape(11.dp), color=if(sel)c.surface else Color.Transparent, shadowElevation=if(sel)1.dp else 0.dp) {
                            Box(contentAlignment=Alignment.Center){ Text(label, color=if(!sel)c.secondaryText else if(v=="expense")c.error else c.success, fontWeight=FontWeight.ExtraBold, fontSize=13.sp) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Surface(modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(16.dp), color=c.background) {
                Row(Modifier.padding(horizontal=16.dp, vertical=11.dp), verticalAlignment=Alignment.CenterVertically) {
                    Text(currencySymbol(state.currency), color=c.mutedText, fontSize=25.sp, fontWeight=FontWeight.ExtraBold)
                    TextField(amount,{ amount=it.filter{ch->ch.isDigit()||ch=='.'} }, modifier=Modifier.weight(1f), placeholder={Text("0.00",color=c.mutedText,fontSize=28.sp,fontWeight=FontWeight.Bold)}, singleLine=true, keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal), textStyle=LocalTextStyle.current.copy(fontSize=28.sp,fontWeight=FontWeight.Bold,color=c.primaryText), colors=TextFieldDefaults.colors(focusedContainerColor=Color.Transparent,unfocusedContainerColor=Color.Transparent,focusedIndicatorColor=Color.Transparent,unfocusedIndicatorColor=Color.Transparent))
                }
            }
            Spacer(Modifier.height(16.dp)); MmLabel(mmText(state,"Category","ক্যাটাগরি")); Spacer(Modifier.height(8.dp))
            relevantCategories.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                    row.forEach { item ->
                        val sel=category?.id==item.id
                        Column(Modifier.weight(1f).clickable{category=item}.padding(vertical=3.dp),horizontalAlignment=Alignment.CenterHorizontally){
                            Surface(modifier=Modifier.size(52.dp),shape=RoundedCornerShape(14.dp),color=catSoft(item.name,c),border=if(sel) androidx.compose.foundation.BorderStroke(1.5.dp,c.action) else null){Box(contentAlignment=Alignment.Center){Icon(catIcon(item.name),null,tint=catStrong(item.name,c),modifier=Modifier.size(20.dp))}}
                            Spacer(Modifier.height(6.dp)); Text(item.name,color=c.primaryText,fontSize=9.5.sp,fontWeight=FontWeight.SemiBold,maxLines=2,lineHeight=10.5.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center,modifier=Modifier.heightIn(min=22.dp))
                        }
                    }
                    repeat(4-row.size){Spacer(Modifier.weight(1f))}
                }
                Spacer(Modifier.height(8.dp))
            }
            MmInput(if(type=="income")mmText(state,"Source","উৎস") else mmText(state,"Merchant","মার্চেন্ট"),merchant,{merchant=it},if(type=="income")"e.g. Acme Corp, Client Name" else "e.g. Whole Foods Market")
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)){Box(Modifier.weight(1f)){MmInput(mmText(state,"Date","তারিখ"),date,{date=it},"YYYY-MM-DD")};Box(Modifier.weight(1f)){MmInput(mmText(state,"Time","সময়"),time,{time=it},"12:00")}}
            Spacer(Modifier.height(14.dp)); MmLabel(mmText(state,"Payment method","পেমেন্ট পদ্ধতি")); Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Card","Cash","Bank Transfer","Wallet").forEach{pm->HtmlChip(pm,payment==pm,onClick={payment=pm})}}
            Spacer(Modifier.height(14.dp)); MmLabel(mmText(state,"Account","অ্যাকাউন্ট")); Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){state.accounts.forEach{acc->HtmlChip(acc.name,account?.id==acc.id,onClick={account=acc})}}
            Spacer(Modifier.height(14.dp)); MmInput(mmText(state,"Notes","নোট"),notes,{notes=it},mmText(state,"Add a note (optional)","নোট লিখুন (ঐচ্ছিক)"),3)
            Spacer(Modifier.height(14.dp)); MmLabel(mmText(state,"Receipt","রসিদ")); Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick={receiptAttached=!receiptAttached},modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(14.dp),border=androidx.compose.foundation.BorderStroke(1.dp,c.border)){Icon(Icons.Filled.PhotoCamera,null,modifier=Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text(if(receiptAttached)mmText(state,"Receipt photo attached","রসিদের ছবি যুক্ত হয়েছে") else mmText(state,"Attach receipt photo","রসিদের ছবি যুক্ত করুন"),fontWeight=FontWeight.Bold)}
            localError?.let{Spacer(Modifier.height(8.dp));Text(it,color=c.error,fontSize=12.sp)}
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)){
                Button(onClick=onDismiss,modifier=Modifier.weight(1f).height(54.dp),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=c.background,contentColor=c.primaryText)){Text(mmText(state,"Cancel","বাতিল"),fontWeight=FontWeight.Bold)}
                Button(onClick={val v=amount.toDoubleOrNull();val a=account;if(v==null||v<=0||a==null){localError=mmText(state,"Enter a valid amount and account.","সঠিক পরিমাণ ও অ্যাকাউন্ট নির্বাচন করুন।");return@Button};saving=true;scope.launch{val req=TransactionRequest(a.id,category?.id,type,v,merchant.ifBlank{null},payment,notes.ifBlank{null},occurredAt=dateToIso(date)?:java.time.Instant.now().toString());val ok=if(initial==null)state.createTransaction(req) else state.updateTransaction(initial.id,req);saving=false;if(ok)onDismiss() else localError=state.error}},enabled=!saving,modifier=Modifier.weight(2f).height(54.dp),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=c.action,contentColor=Color.White)){if(saving)CircularProgressIndicator(Modifier.size(19.dp),strokeWidth=2.dp,color=Color.White) else Text(mmText(state,"Save Transaction","লেনদেন সংরক্ষণ"),fontWeight=FontWeight.ExtraBold)}
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: mmText
// Purpose: Encapsulates the mm Text section of this file.
// -----------------------------------------------------------------------------
@Composable private fun MmLabel(t:String){val c=LocalMoneyMateTokens.current;Text(t,color=c.secondaryText,fontSize=11.5.sp,fontWeight=FontWeight.Bold)}
@Composable private fun MmInput(label:String,value:String,onChange:(String)->Unit,placeholder:String,minLines:Int=1){val c=LocalMoneyMateTokens.current;Column{MmLabel(label);Spacer(Modifier.height(7.dp));TextField(value,onChange,modifier=Modifier.fillMaxWidth(),placeholder={Text(placeholder,color=c.mutedText,fontSize=13.sp)},singleLine=minLines==1,minLines=minLines,shape=RoundedCornerShape(14.dp),colors=TextFieldDefaults.colors(focusedContainerColor=c.background,unfocusedContainerColor=c.background,focusedIndicatorColor=Color.Transparent,unfocusedIndicatorColor=Color.Transparent))}}
private fun mmText(state:MoneyMateState,en:String,bn:String)=if(state.language=="bn")bn else en

// -----------------------------------------------------------------------------
// Section: catSoft
// Purpose: Encapsulates the cat Soft section of this file.
// -----------------------------------------------------------------------------
private fun catSoft(n:String,c:com.moneymate.app.ui.theme.MoneyMateTokens)=when{n.contains("food",true)||n.contains("rent",true)->c.lightWarning;n.contains("grocer",true)||n.contains("salary",true)||n.contains("kids",true)->c.lightSuccess;n.contains("health",true)||n.contains("gift",true)->c.lightError;else->c.lightAction}

// -----------------------------------------------------------------------------
// Section: catStrong
// Purpose: Encapsulates the cat Strong section of this file.
// -----------------------------------------------------------------------------
private fun catStrong(n:String,c:com.moneymate.app.ui.theme.MoneyMateTokens)=when{n.contains("food",true)||n.contains("rent",true)->c.warning;n.contains("grocer",true)||n.contains("salary",true)||n.contains("kids",true)->c.success;n.contains("health",true)||n.contains("gift",true)->c.error;else->c.action}

// -----------------------------------------------------------------------------
// Section: catIcon
// Purpose: Encapsulates the cat Icon section of this file.
// -----------------------------------------------------------------------------
private fun catIcon(n:String)=when{n.contains("food",true)->Icons.Filled.Restaurant;n.contains("grocer",true)->Icons.Filled.ShoppingCart;n.contains("shopping",true)->Icons.Filled.ShoppingBag;n.contains("transport",true)->Icons.Filled.DirectionsCar;n.contains("bill",true)->Icons.Filled.ReceiptLong;n.contains("rent",true)||n.contains("housing",true)->Icons.Filled.Home;n.contains("entertain",true)->Icons.Filled.Movie;n.contains("subscription",true)->Icons.Filled.Subscriptions;n.contains("health",true)->Icons.Filled.Favorite;n.contains("insurance",true)->Icons.Filled.Shield;n.contains("travel",true)->Icons.Filled.Flight;n.contains("education",true)->Icons.Filled.School;n.contains("personal",true)->Icons.Filled.Spa;n.contains("kids",true)||n.contains("family",true)->Icons.Filled.FamilyRestroom;n.contains("pet",true)->Icons.Filled.Pets;n.contains("gift",true)||n.contains("donation",true)->Icons.Filled.VolunteerActivism;n.contains("tax",true)->Icons.Filled.AccountBalance;n.contains("salary",true)->Icons.Filled.Payments;n.contains("freelance",true)->Icons.Filled.LaptopMac;n.contains("business",true)->Icons.Filled.Store;n.contains("investment",true)->Icons.Filled.TrendingUp;n.contains("refund",true)->Icons.Filled.Replay;else->Icons.Filled.Category}


// -----------------------------------------------------------------------------
// Section: TransferDialog
// Purpose: Renders and manages the Transfer Dialog modal UI flow.
// -----------------------------------------------------------------------------
@Composable
fun TransferDialog(state: MoneyMateState, onDismiss: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val scope = rememberCoroutineScope()
    var from by remember { mutableStateOf(state.accounts.firstOrNull()) }
    var to by remember { mutableStateOf(state.accounts.drop(1).firstOrNull()) }
    var amount by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    HtmlSheet("Transfer Money", onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Text("From account", color = c.secondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.accounts.forEach { acc -> HtmlChip(acc.name, from?.id == acc.id, { from = acc }) }
                }
            }
            Column {
                Text("To account", color = c.secondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.accounts.forEach { acc -> HtmlChip(acc.name, to?.id == acc.id, { to = acc }) }
                }
            }
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0.00") },
                prefix = { Text(currencySymbol(state.currency), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            )
            localError?.let { Text(it, color = c.error, fontSize = 12.sp) }
            PrimaryButton(if (saving) "Transferring…" else "Confirm transfer", enabled = !saving) {
                val numeric = amount.toDoubleOrNull()
                if (numeric == null || numeric <= 0 || from == null || to == null || from!!.id == to!!.id) {
                    localError = "Choose two different accounts and a valid amount."
                    return@PrimaryButton
                }
                saving = true
                scope.launch {
                    val ok = state.transfer(TransferRequest(from!!.id, to!!.id, numeric, null, java.time.Instant.now().toString()))
                    saving = false
                    if (ok) onDismiss() else localError = state.error
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: AccountDialog
// Purpose: Renders and manages the Account Dialog modal UI flow.
// -----------------------------------------------------------------------------
@Composable
fun AccountDialog(state: MoneyMateState, existing: Account? = null, onDismiss: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var type by remember { mutableStateOf(existing?.type ?: "bank") }
    var balance by remember { mutableStateOf(existing?.balance ?: "0") }
    var localError by remember { mutableStateOf<String?>(null) }

    HtmlSheet(if (existing == null) "New Account" else "Edit Account", onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SheetField(name, { name = it }, "Account name")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("bank", "cash", "savings", "credit_card").forEach { HtmlChip(it.replace('_', ' ').replaceFirstChar(Char::uppercase), type == it, { type = it }) }
            }
            SheetField(balance, { balance = it }, "Balance", KeyboardType.Decimal)
            localError?.let { Text(it, color = c.error, fontSize = 12.sp) }
            PrimaryButton("Save Account") {
                val numeric = balance.toDoubleOrNull()
                if (name.isBlank() || type.isBlank() || numeric == null) { localError = "Complete all fields."; return@PrimaryButton }
                scope.launch {
                    val body = AccountRequest(name.trim(), type.trim(), numeric, state.currency)
                    val ok = if (existing == null) state.createAccount(body) else state.updateAccount(existing.id, body)
                    if (ok) onDismiss() else localError = state.error
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: BudgetDialog
// Purpose: Renders and manages the Budget Dialog modal UI flow.
// -----------------------------------------------------------------------------
@Composable
fun BudgetDialog(state: MoneyMateState, existing: Budget? = null, onDismiss: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val scope = rememberCoroutineScope()
    val expenseCategories = CategoryCatalog.expenseCategories(state.categories)
    var category by remember { mutableStateOf(expenseCategories.firstOrNull { it.id == existing?.categoryId } ?: expenseCategories.firstOrNull()) }
    var amount by remember { mutableStateOf(existing?.amount ?: "") }
    val now = LocalDate.now()
    var month by remember { mutableStateOf((existing?.month ?: now.monthValue).toString()) }
    var year by remember { mutableStateOf((existing?.year ?: now.year).toString()) }
    var localError by remember { mutableStateOf<String?>(null) }

    HtmlSheet(if (existing == null) "New Budget" else "Edit Budget", onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PickerField("Category", expenseCategories, category, { it.name }) { category = it }
            SheetField(amount, { amount = it }, "Budget limit", KeyboardType.Decimal)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { SheetField(month, { month = it }, "Month", KeyboardType.Number) }
                Box(Modifier.weight(1f)) { SheetField(year, { year = it }, "Year", KeyboardType.Number) }
            }
            localError?.let { Text(it, color = c.error, fontSize = 12.sp) }
            PrimaryButton("Save Budget") {
                val a = amount.toDoubleOrNull(); val m = month.toIntOrNull(); val y = year.toIntOrNull()
                if (category == null || a == null || a <= 0 || m !in 1..12 || y == null) { localError = "Enter valid budget details."; return@PrimaryButton }
                scope.launch {
                    val body = BudgetRequest(category!!.id, a, m!!, y)
                    val ok = if (existing == null) state.createBudget(body) else state.updateBudget(existing.id, body)
                    if (ok) onDismiss() else localError = state.error
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: GoalDialog
// Purpose: Renders and manages the Goal Dialog modal UI flow.
// -----------------------------------------------------------------------------
@Composable
fun GoalDialog(state: MoneyMateState, existing: SavingsGoal? = null, onDismiss: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var amount by remember { mutableStateOf(existing?.targetAmount ?: "") }
    var date by remember { mutableStateOf(existing?.targetDate?.take(10) ?: LocalDate.now().plusMonths(6).toString()) }
    var localError by remember { mutableStateOf<String?>(null) }

    HtmlSheet(if (existing == null) "Create Goal" else "Edit Goal", onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SheetField(name, { name = it }, "Goal name")
            SheetField(amount, { amount = it }, "Target amount", KeyboardType.Decimal)
            SheetField(date, { date = it }, "Target date (YYYY-MM-DD)")
            localError?.let { Text(it, color = c.error, fontSize = 12.sp) }
            PrimaryButton("Save Goal") {
                val numeric = amount.toDoubleOrNull(); val iso = dateToIso(date)
                if (name.isBlank() || numeric == null || numeric <= 0 || iso == null) { localError = "Enter valid goal details."; return@PrimaryButton }
                scope.launch {
                    val body = SavingsGoalRequest(name.trim(), numeric, iso)
                    val ok = if (existing == null) state.createGoal(body) else state.updateGoal(existing.id, body)
                    if (ok) onDismiss() else localError = state.error
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: ContributionDialog
// Purpose: Renders and manages the Contribution Dialog modal UI flow.
// -----------------------------------------------------------------------------
@Composable
fun ContributionDialog(state: MoneyMateState, goal: SavingsGoal, onDismiss: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val scope = rememberCoroutineScope()
    var amount by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    HtmlSheet("Add funds to ${goal.name}", onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SheetField(amount, { amount = it }, "Amount", KeyboardType.Decimal)
            localError?.let { Text(it, color = c.error, fontSize = 12.sp) }
            PrimaryButton("Add Funds") {
                val numeric = amount.toDoubleOrNull()
                if (numeric == null || numeric <= 0) { localError = "Enter a valid amount."; return@PrimaryButton }
                scope.launch { if (state.contributeGoal(goal.id, numeric)) onDismiss() else localError = state.error }
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: BillDialog
// Purpose: Renders and manages the Bill Dialog modal UI flow.
// -----------------------------------------------------------------------------
@Composable
fun BillDialog(state: MoneyMateState, existing: Bill? = null, onDismiss: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var amount by remember { mutableStateOf(existing?.amount ?: "") }
    var date by remember { mutableStateOf(existing?.dueDate?.take(10) ?: LocalDate.now().plusDays(7).toString()) }
    var status by remember { mutableStateOf(existing?.status ?: "upcoming") }
    var localError by remember { mutableStateOf<String?>(null) }

    HtmlSheet(if (existing == null) "Add Bill" else "Edit Bill", onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SheetField(name, { name = it }, "Bill name")
            SheetField(amount, { amount = it }, "Amount", KeyboardType.Decimal)
            SheetField(date, { date = it }, "Due date (YYYY-MM-DD)")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("upcoming", "overdue", "paid").forEach { HtmlChip(it.replaceFirstChar(Char::uppercase), status == it, { status = it }) }
            }
            localError?.let { Text(it, color = c.error, fontSize = 12.sp) }
            PrimaryButton("Save Bill") {
                val numeric = amount.toDoubleOrNull(); val iso = dateToIso(date)
                if (name.isBlank() || numeric == null || numeric <= 0 || iso == null) { localError = "Enter valid bill details."; return@PrimaryButton }
                scope.launch {
                    val body = BillRequest(name.trim(), numeric, iso, status)
                    val ok = if (existing == null) state.createBill(body) else state.updateBill(existing.id, body)
                    if (ok) onDismiss() else localError = state.error
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: LanguageSheet
// Purpose: Renders and manages the Language Sheet modal UI flow.
// -----------------------------------------------------------------------------
@Composable
fun LanguageSheet(state: MoneyMateState, onDismiss: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val scope = rememberCoroutineScope()
    HtmlSheet("Language", onDismiss) {
        listOf("en" to "English", "bn" to "বাংলা (Bangla)").forEachIndexed { index, (code, label) ->
            Row(
                Modifier.fillMaxWidth().clickable {
                    scope.launch {
                        if (state.setLanguage(code)) onDismiss()
                    }
                }.padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, color = c.primaryText, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(
                    if (state.language == code) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (state.language == code) c.action else c.mutedText
                )
            }
            if (index == 0) HorizontalDivider(color = c.divider)
        }
    }
}


// -----------------------------------------------------------------------------
// Section: CurrencySheet
// Purpose: Renders and manages the Currency Sheet modal UI flow.
// -----------------------------------------------------------------------------
@Composable
fun CurrencySheet(state: MoneyMateState, onDismiss: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val scope = rememberCoroutineScope()
    val currencies = listOf(
        "USD" to "US Dollar", "BDT" to "Bangladeshi Taka", "MYR" to "Malaysian Ringgit",
        "EUR" to "Euro", "GBP" to "British Pound", "SGD" to "Singapore Dollar", "INR" to "Indian Rupee"
    )
    HtmlSheet("Currency Manager", onDismiss) {
        Text("Select your preferred currency", color = c.secondaryText, fontSize = 12.5.sp)
        Row(Modifier.padding(top = 4.dp, bottom = 12.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Filled.Info, null, tint = c.mutedText, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
            Text("Display only — amounts are not automatically converted between currencies.", color = c.mutedText, fontSize = 11.5.sp)
        }
        currencies.forEach { (code, name) ->
            Row(
                Modifier.fillMaxWidth().clickable {
                    scope.launch {
                        if (state.setCurrency(code)) onDismiss()
                    }
                }.padding(vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(code, color = c.primaryText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(name, color = c.secondaryText, fontSize = 11.5.sp)
                }
                Icon(
                    if (state.currency == code) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    null,
                    tint = if (state.currency == code) c.action else c.mutedText
                )
            }
            HorizontalDivider(color = c.divider)
        }
    }
}


// -----------------------------------------------------------------------------
// Section: PlansSheet
// Purpose: Renders and manages the Plans Sheet modal UI flow.
// -----------------------------------------------------------------------------
@Composable
fun PlansSheet(state: MoneyMateState, onDismiss: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    var selected by remember { mutableStateOf("yearly") }
    data class Plan(val id:String,val name:String,val price:String,val period:String,val save:String="",val best:Boolean=false)
    val plans=listOf(
        Plan("monthly","Monthly","$5.55","per month"),
        Plan("sixmonth","6 Months","$25.25","per 6 months","Save 24%"),
        Plan("yearly","1 Year","$45.45","per year","Save 32%",true)
    )
    HtmlSheet(mmText(state,"Choose Your Plan","আপনার প্ল্যান বেছে নিন"), onDismiss) {
        Text(mmText(state,"All plans include full access to every MoneyMate feature.","সব প্ল্যানে MoneyMate-এর সব ফিচারের পূর্ণ অ্যাক্সেস থাকবে।"),color=c.secondaryText,fontSize=12.5.sp)
        Spacer(Modifier.height(14.dp))
        plans.forEach { plan ->
            val sel=selected==plan.id || selected==plan.name
            Box(Modifier.fillMaxWidth().padding(vertical=5.dp)){
                Surface(onClick={selected=plan.id},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),color=if(sel)c.lightWarning else c.surface,border=androidx.compose.foundation.BorderStroke(2.dp,if(sel)c.warning else c.divider)){
                    Row(Modifier.padding(horizontal=16.dp,vertical=15.dp),verticalAlignment=Alignment.CenterVertically){
                        Box(Modifier.size(24.dp).background(Color.Transparent,CircleShape).border(androidx.compose.foundation.BorderStroke(2.dp,if(sel)c.warning else c.border),CircleShape),contentAlignment=Alignment.Center){if(sel)Box(Modifier.size(11.dp).background(c.warning,CircleShape))}
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)){Text(plan.name,color=c.primaryText,fontWeight=FontWeight.ExtraBold,fontSize=14.5.sp);if(plan.save.isNotBlank())Text(plan.save,color=c.success,fontSize=11.5.sp,fontWeight=FontWeight.Bold)}
                        Column(horizontalAlignment=Alignment.End){Text(plan.price,color=c.primaryText,fontWeight=FontWeight.ExtraBold,fontSize=15.5.sp);Text(plan.period,color=c.mutedText,fontSize=10.5.sp)}
                    }
                }
                if(plan.best) Surface(shape=RoundedCornerShape(7.dp),color=c.warning,modifier=Modifier.offset(x=16.dp,y=(-7).dp)){Text("BEST VALUE",color=c.brand,fontSize=9.sp,fontWeight=FontWeight.ExtraBold,modifier=Modifier.padding(horizontal=9.dp,vertical=3.dp))}
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick={state.message = mmText(state,"Subscription checkout must be connected to Google Play Billing before release.","রিলিজের আগে সাবস্ক্রিপশন চেকআউট Google Play Billing-এর সাথে সংযুক্ত করতে হবে।");onDismiss()},modifier=Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=c.action,contentColor=Color.White)){Text(mmText(state,"Subscribe Now","এখন সাবস্ক্রাইব করুন"),fontWeight=FontWeight.ExtraBold,fontSize=15.sp)}
        Text(mmText(state,"Purchases must be completed through the configured app-store billing provider.","পেমেন্ট কনফিগার করা অ্যাপ-স্টোর বিলিং প্রোভাইডারের মাধ্যমে সম্পন্ন হবে।"),color=c.mutedText,fontSize=11.sp,modifier=Modifier.align(Alignment.CenterHorizontally).padding(top=12.dp))
    }
}


// -----------------------------------------------------------------------------
// Section: FilterSheet
// Purpose: Renders and manages the Filter Sheet modal UI flow.
// -----------------------------------------------------------------------------
@Composable
fun FilterSheet(
    initialType: String,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit
) {
    val c = LocalMoneyMateTokens.current
    var type by remember { mutableStateOf(initialType.lowercase()) }
    var range by remember { mutableStateOf("all") }
    var sort by remember { mutableStateOf("date-desc") }
    HtmlSheet("Filter & Sort", onDismiss) {
        Text("Type", color = c.primaryText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("all", "income", "expense").forEach { value -> HtmlChip(value.replaceFirstChar(Char::uppercase), type == value, onClick = { type = value }) }
        }
        Spacer(Modifier.height(18.dp))
        Text("Date Range", color = c.primaryText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("all" to "All Time", "today" to "Today", "week" to "This Week").forEach { (value, label) -> HtmlChip(label, range == value, onClick = { range = value }) }
        }
        Row(Modifier.padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("month" to "This Month", "custom" to "Custom Range").forEach { (value, label) -> HtmlChip(label, range == value, onClick = { range = value }) }
        }
        Spacer(Modifier.height(18.dp))
        Text("Sort by", color = c.primaryText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                HtmlChip("Newest first", sort == "date-desc", onClick = { sort = "date-desc" })
                HtmlChip("Oldest first", sort == "date-asc", onClick = { sort = "date-asc" })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                HtmlChip("Highest amount", sort == "amount-desc", onClick = { sort = "amount-desc" })
                HtmlChip("Lowest amount", sort == "amount-asc", onClick = { sort = "amount-asc" })
            }
        }
        Spacer(Modifier.height(18.dp))
        PrimaryButton("Apply filters") { onApply(type); onDismiss() }
    }
}


// -----------------------------------------------------------------------------
// Section: currencySymbol
// Purpose: Encapsulates the currency Symbol section of this file.
// -----------------------------------------------------------------------------
private fun currencySymbol(code: String): String =
    when (code.uppercase()) {
        "USD" -> "$"
        "BDT" -> "৳"
        "MYR" -> "RM"
        "EUR" -> "€"
        "GBP" -> "£"
        "SGD" -> "S$"
        "INR" -> "₹"
        else -> code.uppercase()
    }
