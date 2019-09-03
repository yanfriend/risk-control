futures_info = \
    [
        {"symbol": "QM", "miniTick": 0.025, "multiplier": 500},
        {"symbol": "CL", "miniTick": 0.01, "multiplier": 1000},
        {"symbol": "QO", "miniTick": 0.25, "multiplier": 50},

        # {"symbol": "-Stock Index--", "miniTick": 0.1, "multiplier": 0},
        {"symbol": "ES", "miniTick": 0.25, "multiplier": 50},
        {"symbol": "NQ", "miniTick": 0.25, "multiplier": 20},

        # {"symbol": "-Bond-", "miniTick": 0.1, "multiplier": 0},
        {"symbol": "ZN", "miniTick": 0.015625, "multiplier": 1000},
        {"symbol": "ZB", "miniTick": 0.03125, "multiplier": 1000},
        {"symbol": "ZF", "miniTick": 0.0078125, "multiplier": 1000},
        {"symbol": "ZT", "miniTick": 0.0078125, "multiplier": 2000},

        # {"symbol": "-Forex-", "miniTick": 0.1, "multiplier": 0},
        {"symbol": "EUR", "miniTick": 0.0001, "multiplier": 125000},
        {"symbol": "AUD", "miniTick": 0.0001, "multiplier": 100000},
        {"symbol": "CAD", "miniTick": 0.0001, "multiplier": 100000},
        {"symbol": "CHF", "miniTick": 0.0001, "multiplier": 125000},
        {"symbol": "GBP", "miniTick": 0.0001, "multiplier": 62500},
        {"symbol": "JPY", "miniTick": 0.000001, "multiplier": 12500000},
        {"symbol": "NZD", "miniTick": 0.0001, "multiplier": 100000},

        # {"symbol": "-Agriculture-", "miniTick": 0.1, "multiplier": 0},
        {"symbol": "ZM", "miniTick": 0.1, "multiplier": 100},
        {"symbol": "ZL", "miniTick": 0.0001, "multiplier": 60000},
        {"symbol": "ZS", "miniTick": 0.0025, "multiplier": 5000},
        {"symbol": "ZC", "miniTick": 0.0025, "multiplier": 5000},
        {"symbol": "ZW", "miniTick": 0.25, "multiplier": 50},

        # {"symbol": "-Meat-", "miniTick": 0.1, "multiplier": 0},
        {"symbol": "GF", "miniTick": 2.5E-2, "tickValue": 12.5, "multiplier": 500},
        {"symbol": "LE", "miniTick": 0.00025, "tickValue": 12.5, "multiplier": 40000},
        {"symbol": "HE", "miniTick": 0.00025, "tickValue": 12.5, "multiplier": 40000}
    ]


class Contract:
    def __init__(self, symbol, mini_tick, multiplier):
        self.symbol = symbol
        self.mini_tick = mini_tick
        self.multiplier = multiplier
        self.tick_value = mini_tick * multiplier

    def __repr__(self):
        return 'symbol:{}, mini_tick:{}, tick_value:{}; '.format(self.symbol, self.mini_tick, self.tick_value)


def load_contracts(futures_info):
    return [Contract(fut['symbol'], fut['miniTick'], fut['multiplier']) for fut in futures_info]


if __name__ == '__main__':
    print('run main')
    contracts = load_contracts(futures_info)

    for i, val in enumerate(contracts):
        print(i, val)

    # print(contracts)

    contract_order = int(input("input which contract to calculate:") or '0')
    trade_contract = contracts[contract_order]
    print(trade_contract)

    # ATR
    atr = float(input('please input two week ATR:'))
    atr *= 2
    atr_value = atr / trade_contract.mini_tick * trade_contract.tick_value
    print('atr(*2) value:{}'.format(atr_value))

    # how much loss can take.
    loss = float(input('please input how much loss can afford:'))
    contract_amount = loss / atr_value
    print('You can trade {} contract by stop loss'.format(contract_amount))

    # how much by Kelly formular
    win_rate = float(raw_input('please input winning rate:') or '0.6')
    win_loss = float(raw_input('please input pure profit/loss ratio:') or '2')
    kelly = win_rate - (1 - win_rate) / win_loss
    print('With stop loss, you can put {} of your account value at risk'.format(kelly))

"""
1, Symbol - static
mini tick
multiplier
tick value = mini_tick * multiplier. For example: ES, mini tick = 0.25, multiplier 50, tick value = 0.25*50=12.5$

2, ATR Value (2*atr) - changing
Suppose 2*ATR as stop loss value. => loss dollar amount

3, entry: 
long stop
short stop

4, How many to trade, by loss value: 
Loss $ can take = Account $ * how much percentage to loss
contract amount = $loss / (2*atr * tick value)

5, How many to trade, by Kelly's formula:
contract amount percentage = W% - (1-W%) / (Win amount/Loss amount)

"""
