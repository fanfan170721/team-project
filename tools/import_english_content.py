"""Build the English study bank from the owned reference selection and pinned ECDICT."""
import argparse,csv,json,hashlib,re
from pathlib import Path
p=argparse.ArgumentParser()
p.add_argument("content",type=Path)
p.add_argument("ecdict",type=Path)
p.add_argument("--root",type=Path,default=Path(__file__).resolve().parents[1])
a=p.parse_args()
expected="d1ecbdf9ee2b920afb6f430ca6b6bb9f559588cf0dff86e04bd255f9390843cc"
if hashlib.sha256(a.ecdict.read_bytes()).hexdigest()!=expected: raise ValueError("ECDICT source checksum mismatch")
original=json.loads(a.content.read_text(encoding="utf-8"))["words"]
wanted={w["lemma"] for w in original}
rows={r["word"]:r for r in csv.DictReader(a.ecdict.open(encoding="utf-8-sig",newline="")) if r["word"] in wanted}
overrides=json.loads((a.root/"tools/english-gloss-overrides.json").read_text(encoding="utf-8"))
words=[]
for r in csv.DictReader((a.root/"src/main/resources/study-content.csv").open(encoding="utf-8-sig",newline="")):
 words.append(dict(id="reference-"+r["id"],term=r["lemma"],definition=r["meaning"],level=int(r["level"][1:]),source="Original Java reference"))
known={w["term"].lower() for w in words}
for w in original:
 term=w["lemma"]
 if term.lower() in known or term=="erika": continue
 definition=overrides.get(term)
 source="Project English gloss"
 if not definition:
  senses=[s.strip() for s in rows.get(term,{}).get("definition","").replace("\\n","\n").splitlines() if s.strip()]
  definition=senses[0] if senses else ""
  source="ECDICT ab22ad3 / English definition"
 if not definition or re.search(r"[\u3400-\u9fff]",definition): raise ValueError("Missing English gloss: "+term)
 words.append(dict(id="english-"+term,term=term,definition=definition,level=w["level"],source=source))
 known.add(term.lower())
result={"version":1,"selection":"Original Python vocabulary; one English sense per term; uncalibrated levels",
 "ecdictRevision":"ab22ad36302cafc6349a050e2b60894855208055","ecdictSha256":expected,"words":words}
(a.root/"src/main/resources/english-study-content.json").write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
print("English study words:",len(words),"levels:",sorted({w["level"] for w in words}))
