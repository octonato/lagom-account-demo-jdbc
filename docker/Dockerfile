FROM cassandra

COPY entrypoint-wrap.sh /entrypoint-wrap.sh
ENTRYPOINT ["/entrypoint-wrap.sh"]
CMD ["cassandra", "-f"]