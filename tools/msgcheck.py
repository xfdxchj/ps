import os, re, io
ROOT=r'E:\破碎的地牢\_EndShatteredBuild'
MSG=os.path.join(ROOT,'core/src/main/assets/messages')
SRC=os.path.join(ROOT,'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/endcontent')
BASE='com.shatteredpixel.shatteredpixeldungeon.'
keys=set()
for base,_,files in os.walk(MSG):
    for f in files:
        if not f.endswith('.properties'): continue
        for line in io.open(os.path.join(base,f),encoding='utf-8',errors='ignore'):
            line=line.strip()
            if line and not line.startswith('#') and '=' in line:
                keys.add(line.split('=',1)[0].strip().lower())

def prefix_of(java_path):
    rel=os.path.relpath(java_path, os.path.join(ROOT,'core/src/main/java'))
    rel=rel.replace(chr(92),'/')[:-5]
    if rel.startswith(BASE.replace('.','/')): rel=rel[len(BASE.replace('.','/')):]
    return rel.replace('/','.')

get_re=re.compile(r'Messages\.get\(\s*(this|([A-Za-z0-9_$.]+)\.class)\s*,\s*"([^"]+)"')
direct_re=re.compile(r'Messages\.get\(\s*"([^"]+)"')
imp_re=re.compile(r'^\s*import\s+([A-Za-z0-9_.]+);', re.M)

missing={}
for base,_,files in os.walk(SRC):
    for f in files:
        if not f.endswith('.java'): continue
        p=os.path.join(base,f)
        src=io.open(p,encoding='utf-8',errors='ignore').read()
        top=prefix_of(p)
        imports={}
        for m in imp_re.finditer(src):
            fqn=m.group(1); imports[fqn.split('.')[-1]]=fqn
        for m in get_re.finditer(src):
            owner=m.group(1); lit=m.group(3)
            if owner=='this':
                fqn=top
            else:
                cls=owner[:-6]  # strip ".class"
                if '.' in cls: fqn=cls
                else: fqn=imports.get(cls, cls)
            pref=fqn
            if pref.startswith(BASE): pref=pref[len(BASE):]
            key=(pref+'.'+lit).lower()
            if key not in keys: missing.setdefault(key,[]).append(os.path.basename(p))
        for m in direct_re.finditer(src):
            key=m.group(1).lower()
            if key not in keys: missing.setdefault(key,[]).append(os.path.basename(p))

print('properties key 总数:',len(keys))
print('可疑缺失:',len(missing))
for k,v in sorted(missing.items()):
    print('  MISSING', k, ' <-', v[0])
