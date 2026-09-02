import unittest
import pandas as pd
from analyze import clean, metrics


class MetricsTest(unittest.TestCase):
    def test_metrics_ignore_open_orders_for_mttr_and_sla(self):
        df = pd.DataFrame([
            {"order_no":"1","fault_type":"电气","priority":"HIGH","status":"CLOSED","assignee_name":"甲","created_at":"2026-01-01 08:00","accepted_at":"2026-01-01 09:00","closed_at":"2026-01-01 12:00","sla_deadline":"2026-01-02 08:00","repair_cost":100},
            {"order_no":"2","fault_type":"机械","priority":"MEDIUM","status":"IN_REPAIR","assignee_name":"乙","created_at":"2026-01-02 08:00","accepted_at":"2026-01-02 10:00","closed_at":None,"sla_deadline":"2026-01-04 08:00","repair_cost":None},
        ])
        result = metrics(clean(df))
        self.assertEqual(result["工单总数"], 2)
        self.assertEqual(result["已关闭数"], 1)
        self.assertEqual(result["SLA达标率"], 1.0)
        self.assertEqual(result["平均修复小时MTTR"], 4.0)


if __name__ == "__main__": unittest.main()

