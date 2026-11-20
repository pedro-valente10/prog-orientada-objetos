"""Exporta este projeto Eclipse para um ZIP com o mesmo nome do projeto."""

from pathlib import Path
import re
import xml.etree.ElementTree as ET
from zipfile import ZIP_DEFLATED, ZipFile

projeto = Path(__file__).resolve().parent
nome = ET.parse(str(projeto / '.project')).getroot().findtext('name')
if not nome or not re.fullmatch(r'[0-9]{7,8}(?:-[0-9]{7,8})*', nome):
    raise SystemExit('O nome em .project deve conter uma ou mais matriculas.')
if not (projeto / 'leiame.txt').is_file():
    raise SystemExit('O arquivo leiame.txt deve pertencer ao projeto.')

destino = projeto.parent / (nome + '.zip')
itens = (
    '.project', '.classpath', '.settings', 'leiame.txt', 'README.md',
    'DECISOES_2A_ITERACAO.md', 'DECISOES_3A_ITERACAO.md',
    'REQUISITOS_3A_ITERACAO.md', 'REQUISITOS_4A_ITERACAO.md',
    'exportar_eclipse.py', 'lib', 'src', 'test',
)
with ZipFile(str(destino), 'w', compression=ZIP_DEFLATED, compresslevel=6) as arquivo:
    for item in itens:
        caminho = projeto / item
        if caminho.is_file():
            arquivo.write(str(caminho), nome + '/' + item)
        elif caminho.is_dir():
            for filho in sorted(caminho.rglob('*')):
                if filho.is_file():
                    relativo = filho.relative_to(projeto).as_posix()
                    arquivo.write(str(filho), nome + '/' + relativo)
        else:
            raise SystemExit('Arquivo obrigatorio ausente: ' + item)

with ZipFile(str(destino)) as arquivo:
    erro = arquivo.testzip()
    if erro:
        raise SystemExit('ZIP corrompido: ' + erro)
    nomes = set(arquivo.namelist())
    for item in ('.project', '.classpath', 'leiame.txt', 'src/controller/Main.java'):
        if nome + '/' + item not in nomes:
            raise SystemExit('Arquivo ausente no ZIP: ' + item)
print('ZIP criado:', destino)
print('Arquivos:', len(nomes))